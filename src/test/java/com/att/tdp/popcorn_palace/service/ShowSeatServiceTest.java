package com.att.tdp.popcorn_palace.service;

import com.att.tdp.popcorn_palace.dto.ShowSeatDTO;
import com.att.tdp.popcorn_palace.entity.ShowSeat;
import com.att.tdp.popcorn_palace.repository.ShowSeatRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ShowSeatServiceTest {

    @Mock
    private ShowSeatRepository showSeatRepository;

    @InjectMocks
    private ShowSeatService showSeatService;

    private ShowSeat availableShowSeat;
    private ShowSeat unavailableShowSeat;
    private List<ShowSeat> showSeats;
    private List<Long> seatIds;

    @BeforeEach
    void setUp() {
        // Initialize test data
        availableShowSeat = new ShowSeat(1L, 1L, 1L, true);
        unavailableShowSeat = new ShowSeat(2L, 2L, 3L, false);
        
        showSeats = Arrays.asList(
            availableShowSeat,
            unavailableShowSeat
        );
        
        seatIds = Arrays.asList(1L, 2L, 3L);
    }

    @Test
    void createShowSeats_Success() {
        // Arrange
        when(showSeatRepository.save(any(ShowSeat.class))).thenReturn(availableShowSeat);
        
        // Act
        showSeatService.createShowSeats(1L, seatIds);
        
        // Assert
        verify(showSeatRepository, times(3)).save(any(ShowSeat.class));
    }

    @Test
    void deleteShowSeatsByShowtime_Success() {
        // Arrange
        doNothing().when(showSeatRepository).deleteByShowtimeId(1L);
        
        // Act
        showSeatService.deleteShowSeatsByShowtime(1L);
        
        // Assert
        verify(showSeatRepository).deleteByShowtimeId(1L);
    }

    @Test
    void getSeatsByShowtimeId_Success() {
        // Arrange
        when(showSeatRepository.findByShowtimeId(1L)).thenReturn(showSeats);
        
        // Act
        List<ShowSeatDTO> result = showSeatService.getSeatsByShowtimeId(1L);
        
        // Assert
        assertEquals(2, result.size());
        assertTrue(result.get(0).getIsAvailable());
        assertFalse(result.get(1).getIsAvailable());
    }

    @Test
    void reserveSeat_AvailableSeat_ReturnsTrue() {
        // Arrange
        when(showSeatRepository.findByShowtimeIdAndSeatId(1L, 1L))
                .thenReturn(Optional.of(availableShowSeat));
        when(showSeatRepository.save(any(ShowSeat.class))).thenReturn(unavailableShowSeat);
        
        // Act
        boolean result = showSeatService.reserveSeat(1L, 1L);
        
        // Assert
        assertTrue(result);
        verify(showSeatRepository).save(any(ShowSeat.class));
        
        // Verify the seat is now unavailable
        assertFalse(availableShowSeat.getIsAvailable());
    }

    @Test
    void reserveSeat_UnavailableSeat_ReturnsFalse() {
        // Arrange
        when(showSeatRepository.findByShowtimeIdAndSeatId(2L, 3L))
                .thenReturn(Optional.of(unavailableShowSeat));

        // Act
        boolean result = showSeatService.reserveSeat(2L, 3L);

        // Assert
        assertFalse(result);
        verify(showSeatRepository, never()).save(any(ShowSeat.class));
    }

    @Test
    void reserveSeat_NotFound() {
        // Arrange
        when(showSeatRepository.findByShowtimeIdAndSeatId(1L, 999L))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> showSeatService.reserveSeat(1L, 999L));
        verify(showSeatRepository, never()).save(any(ShowSeat.class));
    }
}
