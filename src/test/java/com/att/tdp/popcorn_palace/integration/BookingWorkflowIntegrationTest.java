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
import com.att.tdp.popcorn_palace.service.SeatService;
import com.att.tdp.popcorn_palace.service.ShowtimeService;
import com.att.tdp.popcorn_palace.service.TheaterService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class BookingWorkflowIntegrationTest {

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

    private static final String TEST_THEATER_NAME = "Integration Test Theater";

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
    public void testCompleteBookingWorkflow() {
        // 1. Add a movie
        MovieDTO movieDTO = new MovieDTO(
                null,
                "Integration Test Movie",
                "Action",
                120,
                8.5,
                2023
        );
        MovieDTO savedMovie = movieService.addMovie(movieDTO);
        assertNotNull(savedMovie.getId());

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
        assertNotNull(savedShowtime.getId());
        
        // 3. Book a seat for the showtime
        UUID userId = UUID.randomUUID();
        
        BookingDTO bookingDTO = new BookingDTO();
        bookingDTO.setShowtimeId(savedShowtime.getId());
        bookingDTO.setSeatNumber(5); // Assuming seat 5 exists in the theater
        bookingDTO.setUserId(userId);
        
        BookingResponseDTO bookingResponse = bookingService.bookTicket(bookingDTO);
        
        // 4. Verify booking was successful
        assertNotNull(bookingResponse);
        assertNotNull(bookingResponse.getBookingId());
        
        // 5. Try to book the same seat again - should fail with SeatAlreadyBookedException
        BookingDTO duplicateBookingDTO = new BookingDTO();
        duplicateBookingDTO.setShowtimeId(savedShowtime.getId());
        duplicateBookingDTO.setSeatNumber(5);
        duplicateBookingDTO.setUserId(UUID.randomUUID());
        
        SeatAlreadyBookedException exception = assertThrows(
            SeatAlreadyBookedException.class, 
            () -> bookingService.bookTicket(duplicateBookingDTO),
            "Expected SeatAlreadyBookedException but got different exception"
        );
        assertTrue(exception.getMessage().contains("Seat 5 is already booked"));
        
        // 6. Book a different seat - should succeed
        BookingDTO differentSeatDTO = new BookingDTO();
        differentSeatDTO.setShowtimeId(savedShowtime.getId());
        differentSeatDTO.setSeatNumber(10); // Different seat
        differentSeatDTO.setUserId(userId);
        
        BookingResponseDTO differentSeatResponse = bookingService.bookTicket(differentSeatDTO);
        assertNotNull(differentSeatResponse);
        assertNotNull(differentSeatResponse.getBookingId());
    }
}
