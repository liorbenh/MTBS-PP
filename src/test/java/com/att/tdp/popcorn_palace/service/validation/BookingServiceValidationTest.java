package com.att.tdp.popcorn_palace.service.validation;

import com.att.tdp.popcorn_palace.dto.BookingDTO;
import com.att.tdp.popcorn_palace.repository.BookingRepository;
import com.att.tdp.popcorn_palace.repository.ShowtimeRepository;
import com.att.tdp.popcorn_palace.repository.TheaterRepository;
import com.att.tdp.popcorn_palace.service.BookingService;
import com.att.tdp.popcorn_palace.service.SeatService;
import com.att.tdp.popcorn_palace.service.ShowSeatService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookingServiceValidationTest {

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

    private BookingDTO validBookingDTO;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        validBookingDTO = new BookingDTO(null, 1L, 5, userId);
    }

    @Test
    void bookTicket_NullShowtimeId_ThrowsException() {
        // Arrange
        BookingDTO bookingDTO = new BookingDTO(null, null, 5, userId);
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> bookingService.bookTicket(bookingDTO));
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void bookTicket_NullSeatNumber_ThrowsException() {
        // Arrange
        BookingDTO bookingDTO = new BookingDTO(null, 1L, null, userId);
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> bookingService.bookTicket(bookingDTO));
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void bookTicket_ZeroSeatNumber_ThrowsException() {
        // Arrange
        BookingDTO bookingDTO = new BookingDTO(null, 1L, 0, userId);
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> bookingService.bookTicket(bookingDTO));
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void bookTicket_NegativeSeatNumber_ThrowsException() {
        // Arrange
        BookingDTO bookingDTO = new BookingDTO(null, 1L, -5, userId);
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> bookingService.bookTicket(bookingDTO));
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void bookTicket_NullUserId_ThrowsException() {
        // Arrange
        BookingDTO bookingDTO = new BookingDTO(null, 1L, 5, null);
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> bookingService.bookTicket(bookingDTO));
        verify(bookingRepository, never()).save(any());
    }
}
