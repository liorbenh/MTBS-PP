package com.att.tdp.popcorn_palace.service;

import com.att.tdp.popcorn_palace.constants.TheaterConstants;
import com.att.tdp.popcorn_palace.dto.SeatDTO;
import com.att.tdp.popcorn_palace.dto.TheaterDTO;
import com.att.tdp.popcorn_palace.entity.Theater;
import com.att.tdp.popcorn_palace.exception.ResourceAlreadyExistsException;
import com.att.tdp.popcorn_palace.exception.ResourceNotFoundException;
import com.att.tdp.popcorn_palace.exception.TheaterLimitExceededException;
import com.att.tdp.popcorn_palace.repository.TheaterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TheaterServiceTest {

    @Mock
    private TheaterRepository theaterRepository;

    @Mock
    private SeatService seatService;

    @InjectMocks
    private TheaterService theaterService;

    private Theater theater1;
    private Theater theater2;
    private TheaterDTO theaterDTO;
    private TheaterDTO theaterDTOWithCustomSeats;

    @BeforeEach
    void setUp() {
        // Initialize test data
        theater1 = new Theater(1L, "Theater 1");
        theater2 = new Theater(2L, "Theater 2", 150);
        
        theaterDTO = new TheaterDTO(null, "New Theater", null);
        theaterDTOWithCustomSeats = new TheaterDTO(null, "Custom Seats Theater", 200);
    }

    @Test
    void getTheaterById_Success() {
        // Arrange
        when(theaterRepository.findById(1L)).thenReturn(Optional.of(theater1));
        
        // Act
        TheaterDTO result = theaterService.getTheaterById(1L);
        
        // Assert
        assertEquals(1L, result.getId());
        assertEquals("Theater 1", result.getName());
        assertEquals(TheaterConstants.DEFAULT_SEATS, result.getNumberOfSeats());
    }

    @Test
    void getTheaterById_NotFound() {
        // Arrange
        when(theaterRepository.findById(999L)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> theaterService.getTheaterById(999L));
    }

    @Test
    void getAllTheaters_Success() {
        // Arrange
        when(theaterRepository.findAll()).thenReturn(Arrays.asList(theater1, theater2));
        
        // Act
        List<TheaterDTO> result = theaterService.getAllTheaters();
        
        // Assert
        assertEquals(2, result.size());
        assertEquals("Theater 1", result.get(0).getName());
        assertEquals("Theater 2", result.get(1).getName());
    }

    @Test
    void addTheater_Success() {
        // Arrange
        Theater newTheater = new Theater(3L, "New Theater");
        when(theaterRepository.count()).thenReturn(2L); // Well below max theaters limit
        when(theaterRepository.findByName("New Theater")).thenReturn(Optional.empty());
        when(theaterRepository.save(any(Theater.class))).thenReturn(newTheater);
        when(seatService.addSeat(any(SeatDTO.class))).thenAnswer(invocation -> {
            SeatDTO seat = invocation.getArgument(0);
            seat.setId(seat.getNumber().longValue());
            return seat;
        });

        // Act
        TheaterDTO result = theaterService.addTheater(theaterDTO);

        // Assert
        assertEquals(3L, result.getId());
        assertEquals("New Theater", result.getName());
        assertEquals(TheaterConstants.DEFAULT_SEATS, result.getNumberOfSeats());
        
        // Verify data flow - seats are created
        verify(seatService, times(TheaterConstants.DEFAULT_SEATS)).addSeat(any(SeatDTO.class));
    }

    @Test
    void addTheater_WithCustomSeats_Success() {
        // Arrange
        Theater newTheater = new Theater(3L, "Custom Seats Theater", 200);
        when(theaterRepository.count()).thenReturn(2L); 
        when(theaterRepository.findByName("Custom Seats Theater")).thenReturn(Optional.empty());
        when(theaterRepository.save(any(Theater.class))).thenReturn(newTheater);
        when(seatService.addSeat(any(SeatDTO.class))).thenAnswer(invocation -> {
            SeatDTO seat = invocation.getArgument(0);
            seat.setId(seat.getNumber().longValue());
            return seat;
        });

        // Act
        TheaterDTO result = theaterService.addTheater(theaterDTOWithCustomSeats);

        // Assert
        assertEquals(3L, result.getId());
        assertEquals("Custom Seats Theater", result.getName());
        assertEquals(200, result.getNumberOfSeats());
        
        // Verify data flow - custom number of seats are created
        verify(seatService, times(200)).addSeat(any(SeatDTO.class));
    }

    @Test
    void addTheater_AlreadyExists() {
        // Arrange
        when(theaterRepository.count()).thenReturn(2L);
        when(theaterRepository.findByName("Theater 1")).thenReturn(Optional.of(theater1));
        
        TheaterDTO existingTheaterDTO = new TheaterDTO(null, "Theater 1", 100);
        
        // Act & Assert
        assertThrows(ResourceAlreadyExistsException.class, () -> theaterService.addTheater(existingTheaterDTO));
        verify(seatService, never()).addSeat(any(SeatDTO.class));
    }

    @Test
    void addTheater_MaxLimit() {
        // Arrange
        when(theaterRepository.count()).thenReturn((long) TheaterConstants.MAX_THEATERS); // At max theaters limit
        when(theaterRepository.findAll()).thenReturn(Collections.nCopies(TheaterConstants.MAX_THEATERS, theater1));

        // Act & Assert
        TheaterLimitExceededException exception = assertThrows(TheaterLimitExceededException.class, 
            () -> theaterService.addTheater(theaterDTO));
        
        assertEquals(TheaterConstants.MAX_THEATERS, exception.getAvailableTheaters().size());
        verify(theaterRepository, never()).save(any(Theater.class));
        verify(seatService, never()).addSeat(any(SeatDTO.class));
    }

    @Test
    void addTheater_NullTheaterName() {
        // Arrange
        TheaterDTO invalidDTO = new TheaterDTO(null, null, 100);
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> theaterService.addTheater(invalidDTO));
        verify(theaterRepository, never()).save(any(Theater.class));
    }

    @Test
    void addTheater_EmptyTheaterName() {
        // Arrange
        TheaterDTO invalidDTO = new TheaterDTO(null, "", 100);
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> theaterService.addTheater(invalidDTO));
        verify(theaterRepository, never()).save(any(Theater.class));
    }

    @Test
    void getTheaterByName_Success() {
        // Arrange
        when(theaterRepository.findByName("Theater 1")).thenReturn(Optional.of(theater1));
        
        // Act
        TheaterDTO result = theaterService.getTheaterByName("Theater 1");
        
        // Assert
        assertEquals(1L, result.getId());
        assertEquals("Theater 1", result.getName());
        assertEquals(100, result.getNumberOfSeats());
    }

    @Test
    void getTheaterByName_NotFound() {
        // Arrange
        when(theaterRepository.findByName("Non-existent")).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> theaterService.getTheaterByName("Non-existent"));
    }
}
