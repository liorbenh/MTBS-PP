package com.att.tdp.popcorn_palace.integration;

import com.att.tdp.popcorn_palace.dto.BookingDTO;
import com.att.tdp.popcorn_palace.dto.BookingResponseDTO;
import com.att.tdp.popcorn_palace.dto.MovieDTO;
import com.att.tdp.popcorn_palace.dto.ShowtimeDTO;
import com.att.tdp.popcorn_palace.dto.TheaterDTO;
import com.att.tdp.popcorn_palace.exception.SeatAlreadyBookedException;
import com.att.tdp.popcorn_palace.repository.BookingRepository;
import com.att.tdp.popcorn_palace.repository.MovieRepository;
import com.att.tdp.popcorn_palace.repository.ShowSeatRepository;
import com.att.tdp.popcorn_palace.repository.ShowtimeRepository;
import com.att.tdp.popcorn_palace.repository.TheaterRepository;
import com.att.tdp.popcorn_palace.service.BookingService;
import com.att.tdp.popcorn_palace.service.MovieService;
import com.att.tdp.popcorn_palace.service.ShowtimeService;
import com.att.tdp.popcorn_palace.service.TheaterService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class ConcurrentBookingIntegrationTest {

    @Autowired
    private MovieService movieService;

    @Autowired
    private ShowtimeService showtimeService;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private TheaterRepository theaterRepository;
    
    @Autowired
    private TheaterService theaterService;
    
    @Autowired
    private MovieRepository movieRepository;
    
    @Autowired
    private ShowtimeRepository showtimeRepository;
    
    @Autowired
    private BookingRepository bookingRepository;
    
    @Autowired
    private ShowSeatRepository showSeatRepository;

    private static final String TEST_THEATER_NAME = "Concurrent Test Theater";
    private static final int TEST_SEAT_NUMBER = 42; // Use a unique seat number for concurrent testing

    @BeforeEach
    public void setup() {
        // Clean up any existing data to avoid test interference
        bookingRepository.deleteAll();
        showSeatRepository.deleteAll();
        showtimeRepository.deleteAll();
        movieRepository.deleteAll();
        
        // Create test theater if it doesn't exist
        if (theaterRepository.findByName(TEST_THEATER_NAME).isEmpty()) {
            TheaterDTO theaterDTO = new TheaterDTO(null, TEST_THEATER_NAME, 100);
            theaterService.addTheater(theaterDTO);
        }
    }

    @Test
    public void testConcurrentBooking() throws InterruptedException {
        // 1. Add a movie
        MovieDTO movieDTO = new MovieDTO(
                null,
                "Concurrent Test Movie",
                "Action",
                120,
                8.5,
                2023
        );
        MovieDTO savedMovie = movieService.addMovie(movieDTO);

        // 2. Add a showtime for the movie
        LocalDateTime startTime = LocalDateTime.now().plusDays(1);
        LocalDateTime endTime = startTime.plusHours(2);
        
        ShowtimeDTO showtimeDTO = new ShowtimeDTO(
                null,
                12.50,
                savedMovie.getId(),
                TEST_THEATER_NAME,
                startTime,
                endTime
        );
        
        ShowtimeDTO savedShowtime = showtimeService.addShowtime(showtimeDTO);
        
        // 3. Set up concurrent booking attempt
        final CountDownLatch startLatch = new CountDownLatch(1);
        final CountDownLatch completionLatch = new CountDownLatch(2); // Add completion latch
        final AtomicReference<BookingResponseDTO> booking1Result = new AtomicReference<>();
        final AtomicReference<BookingResponseDTO> booking2Result = new AtomicReference<>();
        final AtomicReference<Exception> thread1Exception = new AtomicReference<>();
        final AtomicReference<Exception> thread2Exception = new AtomicReference<>();

        // Create two different user IDs for the concurrent booking
        final UUID userId1 = UUID.randomUUID();
        final UUID userId2 = UUID.randomUUID();
        
        // Prepare booking DTOs
        final BookingDTO bookingDTO1 = new BookingDTO();
        bookingDTO1.setShowtimeId(savedShowtime.getId());
        bookingDTO1.setSeatNumber(TEST_SEAT_NUMBER);
        bookingDTO1.setUserId(userId1);
        
        final BookingDTO bookingDTO2 = new BookingDTO();
        bookingDTO2.setShowtimeId(savedShowtime.getId());
        bookingDTO2.setSeatNumber(TEST_SEAT_NUMBER);  // Same seat number!
        bookingDTO2.setUserId(userId2);
        
        // Create two threads to attempt concurrent booking
        Thread thread1 = new Thread(() -> {
            try {
                // Wait for signal to start
                startLatch.await();
                BookingResponseDTO response = bookingService.bookTicket(bookingDTO1);
                booking1Result.set(response);
            } catch (Exception e) {
                thread1Exception.set(e);
            } finally {
                completionLatch.countDown();
            }
        }, "BookingThread-1");
        
        Thread thread2 = new Thread(() -> {
            try {
                // Wait for signal to start
                startLatch.await();
                BookingResponseDTO response = bookingService.bookTicket(bookingDTO2);
                booking2Result.set(response);
            } catch (Exception e) {
                thread2Exception.set(e);
            } finally {
                completionLatch.countDown();
            }
        }, "BookingThread-2");
        
        // Start both threads
        thread1.start();
        thread2.start();
        
        // Signal threads to start execution
        startLatch.countDown();
        
        // Wait for both threads to complete with timeout
        boolean completed = completionLatch.await(10, TimeUnit.SECONDS);
        assertTrue(completed, "Test timed out waiting for booking threads to complete");
        
        // 4. Verify the results
        // Check that exactly one booking succeeded and one failed
        boolean thread1Succeeded = booking1Result.get() != null && thread1Exception.get() == null;
        boolean thread2Succeeded = booking2Result.get() != null && thread2Exception.get() == null;
        
        // One and only one thread should have succeeded
        assertTrue(thread1Succeeded != thread2Succeeded, 
                "Exactly one thread should succeed, but thread1Success=" + thread1Succeeded + 
                ", thread2Success=" + thread2Succeeded);
        
        // The failed thread should have thrown SeatAlreadyBookedException
        Exception failureException = thread1Succeeded ? thread2Exception.get() : thread1Exception.get();
        assertNotNull(failureException, "The failing thread should have an exception");
        assertTrue(failureException instanceof SeatAlreadyBookedException, 
                "Failure should be due to seat already booked, but was: " + failureException.getClass().getName());
        
        // 5. Verify the seat is now booked by checking repository
        boolean seatIsBooked = bookingRepository.existsByShowtimeIdAndSeatNumber(
                savedShowtime.getId(), TEST_SEAT_NUMBER);
        assertTrue(seatIsBooked, "Seat should be marked as booked in the repository");
        
        // 6. Verify only one booking exists for this seat (using findAll instead of count)
        long count = bookingRepository.findAll().stream()
                .filter(booking -> booking.getShowtimeId().equals(savedShowtime.getId()) 
                        && booking.getSeatNumber().equals(TEST_SEAT_NUMBER))
                .count();
        assertEquals(1, count, "There should be exactly one booking for this seat");
        
        // 7. Verify which user successfully booked the seat (using findAll)
        UUID expectedBooker = thread1Succeeded ? userId1 : userId2;
        UUID actualBooker = bookingRepository.findAll().stream()
                .filter(booking -> booking.getShowtimeId().equals(savedShowtime.getId()) 
                        && booking.getSeatNumber().equals(TEST_SEAT_NUMBER))
                .map(booking -> booking.getUserId())
                .findFirst()
                .orElse(null);
        assertEquals(expectedBooker, actualBooker, "The correct user should be assigned the booking");
    }
}
