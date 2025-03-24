package com.att.tdp.popcorn_palace.service;

import com.att.tdp.popcorn_palace.dto.SeatDTO;
import com.att.tdp.popcorn_palace.dto.ShowtimeDTO;
import com.att.tdp.popcorn_palace.dto.TheaterDTO;
import com.att.tdp.popcorn_palace.entity.Showtime;
import com.att.tdp.popcorn_palace.exception.ResourceNotFoundException;
import com.att.tdp.popcorn_palace.exception.ShowtimeOverlapException;
import com.att.tdp.popcorn_palace.repository.BookingRepository;
import com.att.tdp.popcorn_palace.repository.MovieRepository;
import com.att.tdp.popcorn_palace.repository.ShowtimeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ShowtimeServiceTest {

    @Mock
    private ShowtimeRepository showtimeRepository;

    @Mock
    private MovieRepository movieRepository;
    
    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private TheaterService theaterService;

    @Mock
    private SeatService seatService;

    @Mock
    private ShowSeatService showSeatService;

    @InjectMocks
    private ShowtimeService showtimeService;

    private Showtime showtime;
    private ShowtimeDTO showtimeDTO;
    private TheaterDTO theaterDTO;
    private List<SeatDTO> seatDTOs;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    @BeforeEach
    void setUp() {
        // Initialize test data
        startTime = LocalDateTime.now().plusDays(1);
        endTime = startTime.plusHours(2);
        
        showtime = new Showtime(1L, 12.50, 1L, "Theater 1", startTime, endTime);
        showtimeDTO = new ShowtimeDTO(null, 12.50, 1L, "Theater 1", startTime, endTime);
        
        theaterDTO = new TheaterDTO(1L, "Theater 1", 100);
        
        seatDTOs = IntStream.rangeClosed(1, 100)
                .mapToObj(i -> new SeatDTO((long) i, 1L, i))
                .collect(Collectors.toList());
    }

    @Test
    void getShowtimeById_Success() {
        // Arrange
        when(showtimeRepository.findById(1L)).thenReturn(Optional.of(showtime));
        
        // Act
        ShowtimeDTO result = showtimeService.getShowtimeById(1L);
        
        // Assert
        assertEquals(1L, result.getId());
        assertEquals(12.50, result.getPrice());
        assertEquals("Theater 1", result.getTheater());
        assertEquals(startTime, result.getStartTime());
    }

    @Test
    void getShowtimeById_NotFound() {
        // Arrange
        when(showtimeRepository.findById(999L)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> showtimeService.getShowtimeById(999L));
    }

    @Test
    void getAllShowtimes_Success() {
        // Arrange
        List<Showtime> showtimes = Arrays.asList(
            new Showtime(1L, 12.50, 1L, "Theater 1", startTime, endTime),
            new Showtime(2L, 15.00, 2L, "Theater 2", startTime.plusDays(1), endTime.plusDays(1))
        );
        
        when(showtimeRepository.findAll()).thenReturn(showtimes);
        
        // Act
        List<ShowtimeDTO> result = showtimeService.getAllShowtimes();
        
        // Assert
        assertEquals(2, result.size());
        assertEquals("Theater 1", result.get(0).getTheater());
        assertEquals("Theater 2", result.get(1).getTheater());
    }

    @Test
    void addShowtime_Success() {
        // Arrange
        when(movieRepository.existsById(1L)).thenReturn(true);
        when(showtimeRepository.findByTheaterAndTimeRange(anyString(), any(), any()))
                .thenReturn(new ArrayList<>());
        when(theaterService.getTheaterByName("Theater 1")).thenReturn(theaterDTO);
        when(seatService.getSeatsByTheater(1L)).thenReturn(seatDTOs);
        
        Showtime savedShowtime = new Showtime(1L, 12.50, 1L, "Theater 1", startTime, endTime);
        when(showtimeRepository.save(any(Showtime.class))).thenReturn(savedShowtime);
        
        // Act
        ShowtimeDTO result = showtimeService.addShowtime(showtimeDTO);
        
        // Assert
        assertEquals(1L, result.getId());
        assertEquals(12.50, result.getPrice());
        assertEquals("Theater 1", result.getTheater());
        
        // Verify data flow - show seats are created
        verify(showSeatService).createShowSeats(eq(1L), any());
    }

    @Test
    void addShowtime_EndTimeBeforeStartTime() {
        // Arrange
        ShowtimeDTO invalidShowtime = new ShowtimeDTO(
            null, 12.50, 1L, "Theater 1", endTime, startTime); // End time before start time
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> showtimeService.addShowtime(invalidShowtime));
        verify(showtimeRepository, never()).save(any(Showtime.class));
    }

    @Test
    void addShowtime_MovieNotFound() {
        // Arrange
        when(movieRepository.existsById(1L)).thenReturn(false);
        
        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> showtimeService.addShowtime(showtimeDTO));
        verify(showtimeRepository, never()).save(any(Showtime.class));
    }

    @Test
    void addShowtime_TheaterNotFound() {
        // Arrange
        when(movieRepository.existsById(1L)).thenReturn(true);
        when(showtimeRepository.findByTheaterAndTimeRange(anyString(), any(), any()))
                .thenReturn(new ArrayList<>());
        when(theaterService.getTheaterByName("Theater 1"))
                .thenThrow(new ResourceNotFoundException("Theater not found with name: Theater 1"));

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> showtimeService.addShowtime(showtimeDTO));
        verify(showtimeRepository, never()).save(any(Showtime.class));
    }

    @Test
    void addShowtime_Overlap() {
        // Arrange
        when(movieRepository.existsById(1L)).thenReturn(true);
        
        List<Showtime> overlappingShowtimes = Arrays.asList(
            new Showtime(2L, 15.00, 2L, "Theater 1", startTime.minusHours(1), endTime.plusHours(1))
        );
        
        when(showtimeRepository.findByTheaterAndTimeRange(eq("Theater 1"), any(), any()))
                .thenReturn(overlappingShowtimes);
        
        // Act & Assert
        assertThrows(ShowtimeOverlapException.class, () -> showtimeService.addShowtime(showtimeDTO));
        verify(showtimeRepository, never()).save(any(Showtime.class));
    }

    @Test
    void updateShowtime_Success() {
        // Arrange
        when(showtimeRepository.findById(1L)).thenReturn(Optional.of(showtime));
        when(movieRepository.existsById(1L)).thenReturn(true);
        when(showtimeRepository.findByTheaterAndTimeRange(anyString(), any(), any()))
                .thenReturn(new ArrayList<>());
        
        ShowtimeDTO updatedDTO = new ShowtimeDTO(
            1L, 15.00, 1L, "Theater 1", startTime.plusHours(1), endTime.plusHours(1));
        
        Showtime updatedShowtime = new Showtime(
            1L, 15.00, 1L, "Theater 1", startTime.plusHours(1), endTime.plusHours(1));
        
        when(showtimeRepository.save(any(Showtime.class))).thenReturn(updatedShowtime);
        
        // Act
        ShowtimeDTO result = showtimeService.updateShowtime(1L, updatedDTO);
        
        // Assert
        assertEquals(1L, result.getId());
        assertEquals(15.00, result.getPrice());
        assertEquals(startTime.plusHours(1), result.getStartTime());
        assertEquals(endTime.plusHours(1), result.getEndTime());
    }

    @Test
    void updateShowtime_NotFound() {
        // Arrange
        when(showtimeRepository.findById(999L)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(ResourceNotFoundException.class, 
                     () -> showtimeService.updateShowtime(999L, showtimeDTO));
        verify(showtimeRepository, never()).save(any(Showtime.class));
    }

    @Test
    void updateShowtime_MovieNotFound() {
        // Arrange
        when(showtimeRepository.findById(1L)).thenReturn(Optional.of(showtime));
        when(movieRepository.existsById(1L)).thenReturn(false);
        
        // Act & Assert
        assertThrows(ResourceNotFoundException.class, 
                     () -> showtimeService.updateShowtime(1L, showtimeDTO));
        verify(showtimeRepository, never()).save(any(Showtime.class));
    }

    @Test
    void updateShowtime_TheaterChanged_NotFound() {
        // Arrange
        when(showtimeRepository.findById(1L)).thenReturn(Optional.of(showtime));
        when(movieRepository.existsById(1L)).thenReturn(true);
        
        ShowtimeDTO changedTheaterDTO = new ShowtimeDTO(
            1L, 12.50, 1L, "New Theater", startTime, endTime);
        
        when(theaterService.getTheaterByName("New Theater"))
                .thenThrow(new ResourceNotFoundException("Theater not found with name: New Theater"));
        
        // Act & Assert
        assertThrows(ResourceNotFoundException.class, 
                     () -> showtimeService.updateShowtime(1L, changedTheaterDTO));
        verify(showtimeRepository, never()).save(any(Showtime.class));
    }

    @Test
    void updateShowtime_Overlap() {
        // Arrange
        when(showtimeRepository.findById(1L)).thenReturn(Optional.of(showtime));
        when(movieRepository.existsById(1L)).thenReturn(true);
        
        List<Showtime> overlappingShowtimes = Arrays.asList(
            new Showtime(2L, 15.00, 2L, "Theater 1", startTime.minusHours(1), endTime.plusHours(1))
        );
        
        when(showtimeRepository.findByTheaterAndTimeRange(eq("Theater 1"), any(), any()))
                .thenReturn(overlappingShowtimes);
        
        // Act & Assert
        assertThrows(ShowtimeOverlapException.class, 
                     () -> showtimeService.updateShowtime(1L, showtimeDTO));
        verify(showtimeRepository, never()).save(any(Showtime.class));
    }

    @Test
    void deleteShowtime_Success() {
        // Arrange
        when(showtimeRepository.existsById(1L)).thenReturn(true);
        doNothing().when(bookingRepository).deleteByShowtimeId(1L);
        doNothing().when(showSeatService).deleteShowSeatsByShowtime(1L);
        doNothing().when(showtimeRepository).deleteById(1L);
        
        // Act
        showtimeService.deleteShowtime(1L);
        
        // Assert
        // Verify data flow - related bookings and show seats are deleted first
        verify(bookingRepository).deleteByShowtimeId(1L);
        verify(showSeatService).deleteShowSeatsByShowtime(1L);
        verify(showtimeRepository).deleteById(1L);
    }

    @Test
    void deleteShowtime_NotFound() {
        // Arrange
        when(showtimeRepository.existsById(999L)).thenReturn(false);
        
        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> showtimeService.deleteShowtime(999L));
        verify(showSeatService, never()).deleteShowSeatsByShowtime(anyLong());
        verify(showtimeRepository, never()).deleteById(anyLong());
    }

    @Test
    void isShowtimeOverlapping_True() {
        // Arrange
        List<Showtime> overlappingShowtimes = Arrays.asList(
            new Showtime(2L, 15.00, 2L, "Theater 1", startTime.minusHours(1), endTime.plusHours(1))
        );
        
        when(showtimeRepository.findByTheaterAndTimeRange(eq("Theater 1"), any(), any()))
                .thenReturn(overlappingShowtimes);
        
        // Act
        boolean result = showtimeService.isShowtimeOverlapping("Theater 1", startTime, endTime);
        
        // Assert
        assertTrue(result);
    }

    @Test
    void isShowtimeOverlapping_False() {
        // Arrange
        when(showtimeRepository.findByTheaterAndTimeRange(eq("Theater 1"), any(), any()))
                .thenReturn(new ArrayList<>());
        
        // Act
        boolean result = showtimeService.isShowtimeOverlapping("Theater 1", startTime, endTime);
        
        // Assert
        assertFalse(result);
    }
}
