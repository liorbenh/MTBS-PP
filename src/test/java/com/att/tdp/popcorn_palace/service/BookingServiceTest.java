package com.att.tdp.popcorn_palace.service;

import com.att.tdp.popcorn_palace.dto.BookingDTO;
import com.att.tdp.popcorn_palace.dto.BookingResponseDTO;
import com.att.tdp.popcorn_palace.dto.ShowSeatDTO;
import com.att.tdp.popcorn_palace.entity.Booking;
import com.att.tdp.popcorn_palace.entity.Seat;
import com.att.tdp.popcorn_palace.entity.Showtime;
import com.att.tdp.popcorn_palace.entity.Theater;
import com.att.tdp.popcorn_palace.exception.ResourceNotFoundException;
import com.att.tdp.popcorn_palace.exception.SeatAlreadyBookedException;
import com.att.tdp.popcorn_palace.repository.BookingRepository;
import com.att.tdp.popcorn_palace.repository.ShowtimeRepository;
import com.att.tdp.popcorn_palace.repository.TheaterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private ShowtimeRepository showtimeRepository;

    @Mock
    private TheaterRepository theaterRepository;

    @Mock
    private SeatService seatService;

    @Mock
    private ShowSeatService showSeatService;

    @InjectMocks
    private BookingService bookingService;

    private UUID userId;
    private Long showtimeId;
    private BookingDTO bookingDTO;
    private Showtime showtime;
    private Theater theater;
    private Seat seat;
    private ShowSeatDTO showSeatDTO;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        showtimeId = 1L;
        
        // Setup booking DTO
        bookingDTO = new BookingDTO();
        bookingDTO.setShowtimeId(showtimeId);
        bookingDTO.setSeatNumber(10);
        bookingDTO.setUserId(userId);
        
        // Setup showtime
        showtime = new Showtime();
        showtime.setId(showtimeId);
        showtime.setTheater("Cinema City");
        showtime.setMovieId(1L);
        showtime.setStartTime(LocalDateTime.now());
        showtime.setEndTime(LocalDateTime.now().plusHours(2));
        showtime.setPrice(12.50);
        
        // Setup theater
        theater = new Theater();
        theater.setId(1L);
        theater.setName("Cinema City");
        theater.setNumberOfSeats(100);
        
        // Setup seat
        seat = new Seat();
        seat.setId(1L);
        seat.setTheaterId(1L);
        seat.setNumber(10);
        
        // Setup show seat DTO
        showSeatDTO = new ShowSeatDTO();
        showSeatDTO.setId(1L);
        showSeatDTO.setShowtimeId(showtimeId);
        showSeatDTO.setSeatId(1L);
        showSeatDTO.setIsAvailable(true);
    }

    @Test
    void bookTicket_Success() {
        // Arrange
        when(showtimeRepository.findById(showtimeId)).thenReturn(Optional.of(showtime));
        when(theaterRepository.findByName("Cinema City")).thenReturn(Optional.of(theater));
        when(seatService.findByTheaterIdAndNumber(theater.getId(), 10)).thenReturn(seat);
        when(bookingRepository.existsByShowtimeIdAndSeatNumber(showtimeId, 10)).thenReturn(false);
        when(showSeatService.reserveSeat(showtimeId, seat.getId())).thenReturn(true);
        
        UUID bookingId = UUID.randomUUID();
        Booking savedBooking = new Booking();
        savedBooking.setBookingId(bookingId);
        savedBooking.setShowtimeId(showtimeId);
        savedBooking.setSeatNumber(10);
        savedBooking.setUserId(userId);
        
        when(bookingRepository.save(any(Booking.class))).thenReturn(savedBooking);
        
        // Act
        BookingResponseDTO result = bookingService.bookTicket(bookingDTO);
        
        // Assert
        assertNotNull(result);
        assertEquals(bookingId, result.getBookingId());
        verify(showSeatService).reserveSeat(showtimeId, seat.getId());
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    void bookTicket_ShowtimeNotFound() {
        // Arrange
        when(showtimeRepository.findById(999L)).thenReturn(Optional.empty());
        
        bookingDTO.setShowtimeId(999L);
        
        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> bookingService.bookTicket(bookingDTO));
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void bookTicket_TheaterNotFound() {
        // Arrange
        when(showtimeRepository.findById(showtimeId)).thenReturn(Optional.of(showtime));
        when(theaterRepository.findByName("Cinema City")).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> bookingService.bookTicket(bookingDTO));
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void bookTicket_InvalidSeatNumber() {
        // Arrange
        when(showtimeRepository.findById(showtimeId)).thenReturn(Optional.of(showtime));
        when(theaterRepository.findByName("Cinema City")).thenReturn(Optional.of(theater));
        
        // Test with seat number = 0
        bookingDTO.setSeatNumber(0);
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> bookingService.bookTicket(bookingDTO));
        verify(bookingRepository, never()).save(any(Booking.class));
        
        // Test with seat number > number of seats
        bookingDTO.setSeatNumber(101);
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> bookingService.bookTicket(bookingDTO));
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void bookTicket_SeatAlreadyInRepository() {
        // Arrange
        when(showtimeRepository.findById(showtimeId)).thenReturn(Optional.of(showtime));
        when(theaterRepository.findByName("Cinema City")).thenReturn(Optional.of(theater));
        when(bookingRepository.existsByShowtimeIdAndSeatNumber(showtimeId, 10)).thenReturn(true);
        
        // Act & Assert
        assertThrows(SeatAlreadyBookedException.class, () -> bookingService.bookTicket(bookingDTO));
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void bookTicket_ReservationFailed() {
        // Arrange
        when(showtimeRepository.findById(showtimeId)).thenReturn(Optional.of(showtime));
        when(theaterRepository.findByName("Cinema City")).thenReturn(Optional.of(theater));
        when(seatService.findByTheaterIdAndNumber(theater.getId(), 10)).thenReturn(seat);
        when(bookingRepository.existsByShowtimeIdAndSeatNumber(showtimeId, 10)).thenReturn(false);
        
        // Make reservation fail
        when(showSeatService.reserveSeat(showtimeId, seat.getId())).thenReturn(false);
        
        // Act & Assert
        assertThrows(SeatAlreadyBookedException.class, () -> bookingService.bookTicket(bookingDTO));
        verify(bookingRepository, never()).save(any(Booking.class));
    }
}
