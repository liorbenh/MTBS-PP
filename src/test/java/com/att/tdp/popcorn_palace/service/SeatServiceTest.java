package com.att.tdp.popcorn_palace.service;

import com.att.tdp.popcorn_palace.dto.SeatDTO;
import com.att.tdp.popcorn_palace.entity.Seat;
import com.att.tdp.popcorn_palace.repository.SeatRepository;
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
public class SeatServiceTest {

    @Mock
    private SeatRepository seatRepository;

    @InjectMocks
    private SeatService seatService;

    private Seat seat1;
    private Seat seat2;
    private SeatDTO seatDTO;
    private List<Seat> seats;

    @BeforeEach
    void setUp() {
        // Initialize test data
        seat1 = new Seat(1L, 1L, 1);
        seat2 = new Seat(2L, 1L, 2);
        
        seatDTO = new SeatDTO(null, 1L, 3);
        
        seats = Arrays.asList(seat1, seat2);
    }

    @Test
    void getSeatsByTheater_Success() {
        // Arrange
        when(seatRepository.findByTheaterId(1L)).thenReturn(seats);
        
        // Act
        List<SeatDTO> result = seatService.getSeatsByTheater(1L);
        
        // Assert
        assertEquals(2, result.size());
        assertEquals(1, result.get(0).getNumber());
        assertEquals(2, result.get(1).getNumber());
    }

    @Test
    void addSeat_Success() {
        // Arrange
        Seat newSeat = new Seat(3L, 1L, 3);
        when(seatRepository.save(any(Seat.class))).thenReturn(newSeat);
        
        // Act
        SeatDTO result = seatService.addSeat(seatDTO);
        
        // Assert
        assertEquals(3L, result.getId());
        assertEquals(1L, result.getTheaterId());
        assertEquals(3, result.getNumber());
    }

    @Test
    void findByTheaterIdAndNumber_Success() {
        // Arrange
        when(seatRepository.findByTheaterIdAndNumber(1L, 1)).thenReturn(Optional.of(seat1));
        
        // Act
        Seat result = seatService.findByTheaterIdAndNumber(1L, 1);
        
        // Assert
        assertEquals(1L, result.getId());
        assertEquals(1L, result.getTheaterId());
        assertEquals(1, result.getNumber());
    }

    @Test
    void findByTheaterIdAndNumber_NotFound() {
        // Arrange
        when(seatRepository.findByTheaterIdAndNumber(1L, 999)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> seatService.findByTheaterIdAndNumber(1L, 999));
    }
}
