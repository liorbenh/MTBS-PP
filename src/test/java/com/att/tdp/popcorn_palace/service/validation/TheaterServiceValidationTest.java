package com.att.tdp.popcorn_palace.service.validation;

import com.att.tdp.popcorn_palace.constants.TheaterConstants;
import com.att.tdp.popcorn_palace.dto.TheaterDTO;
import com.att.tdp.popcorn_palace.entity.Theater;
import com.att.tdp.popcorn_palace.exception.TheaterLimitExceededException;
import com.att.tdp.popcorn_palace.repository.TheaterRepository;
import com.att.tdp.popcorn_palace.service.SeatService;
import com.att.tdp.popcorn_palace.service.TheaterService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TheaterServiceValidationTest {

    @Mock
    private TheaterRepository theaterRepository;

    @Mock
    private SeatService seatService;

    @InjectMocks
    private TheaterService theaterService;

    private TheaterDTO validTheaterDTO;

    @BeforeEach
    void setUp() {
        validTheaterDTO = new TheaterDTO(null, "Test Theater", 100);
    }

    @Test
    void addTheater_NullTheater_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> theaterService.addTheater(null));
        verify(theaterRepository, never()).save(any());
    }

    @Test
    void addTheater_NullName_ThrowsException() {
        // Arrange
        TheaterDTO theaterDTO = new TheaterDTO(null, null, 100);
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> theaterService.addTheater(theaterDTO));
        verify(theaterRepository, never()).save(any());
    }

    @Test
    void addTheater_EmptyName_ThrowsException() {
        // Arrange
        TheaterDTO theaterDTO = new TheaterDTO(null, "", 100);
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> theaterService.addTheater(theaterDTO));
        verify(theaterRepository, never()).save(any());
    }

    @Test
    void addTheater_BlankName_ThrowsException() {
        // Arrange
        TheaterDTO theaterDTO = new TheaterDTO(null, "   ", 100);
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> theaterService.addTheater(theaterDTO));
        verify(theaterRepository, never()).save(any());
    }

    @Test
    void addTheater_ZeroSeats_ThrowsException() {
        // Arrange
        TheaterDTO theaterDTO = new TheaterDTO(null, "Test Theater", 0);
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> theaterService.addTheater(theaterDTO));
        verify(theaterRepository, never()).save(any());
    }

    @Test
    void addTheater_NegativeSeats_ThrowsException() {
        // Arrange
        TheaterDTO theaterDTO = new TheaterDTO(null, "Test Theater", -10);
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> theaterService.addTheater(theaterDTO));
        verify(theaterRepository, never()).save(any());
    }

    @Test
    void addTheater_ExceedsMaxTheaters_ThrowsException() {
        // Arrange
        when(theaterRepository.count()).thenReturn((long) TheaterConstants.MAX_THEATERS);
        when(theaterRepository.findAll()).thenReturn(
            Collections.nCopies(TheaterConstants.MAX_THEATERS, new Theater())
        );
        
        // Act & Assert
        assertThrows(TheaterLimitExceededException.class, () -> theaterService.addTheater(validTheaterDTO));
        verify(theaterRepository, never()).save(any());
    }

    @Test
    void addTheater_AlreadyExists_ThrowsException() {
        // Arrange
        when(theaterRepository.count()).thenReturn(5L); // Below limit
        when(theaterRepository.findByName("Test Theater")).thenReturn(Optional.of(new Theater()));
        
        // Act & Assert
        assertThrows(Exception.class, () -> theaterService.addTheater(validTheaterDTO));
        verify(theaterRepository, never()).save(any());
    }
}
