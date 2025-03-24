package com.att.tdp.popcorn_palace.service.validation;

import com.att.tdp.popcorn_palace.dto.ShowtimeDTO;
import com.att.tdp.popcorn_palace.repository.BookingRepository;
import com.att.tdp.popcorn_palace.repository.MovieRepository;
import com.att.tdp.popcorn_palace.repository.ShowtimeRepository;
import com.att.tdp.popcorn_palace.service.SeatService;
import com.att.tdp.popcorn_palace.service.ShowSeatService;
import com.att.tdp.popcorn_palace.service.ShowtimeService;
import com.att.tdp.popcorn_palace.service.TheaterService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ShowtimeServiceValidationTest {

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

    private ShowtimeDTO validShowtimeDTO;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    @BeforeEach
    void setUp() {
        startTime = LocalDateTime.now().plusDays(1);
        endTime = startTime.plusHours(2);
        
        validShowtimeDTO = new ShowtimeDTO(
            null,
            12.50,
            1L,
            "Theater 1",
            startTime,
            endTime
        );
    }

    @Test
    void addShowtime_NullShowtime_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> showtimeService.addShowtime(null));
        verify(showtimeRepository, never()).save(any());
    }

    @Test
    void addShowtime_NullMovieId_ThrowsException() {
        // Arrange
        ShowtimeDTO showtimeDTO = new ShowtimeDTO(
            null,
            12.50,
            null, // Null movie ID
            "Theater 1",
            startTime,
            endTime
        );
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> showtimeService.addShowtime(showtimeDTO));
        verify(showtimeRepository, never()).save(any());
    }

    @Test
    void addShowtime_NullPrice_ThrowsException() {
        // Arrange
        ShowtimeDTO showtimeDTO = new ShowtimeDTO(
            null,
            null, // Null price
            1L,
            "Theater 1",
            startTime,
            endTime
        );
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> showtimeService.addShowtime(showtimeDTO));
        verify(showtimeRepository, never()).save(any());
    }

    @Test
    void addShowtime_ZeroPrice_ThrowsException() {
        // Arrange
        ShowtimeDTO showtimeDTO = new ShowtimeDTO(
            null,
            0.0, // Zero price
            1L,
            "Theater 1",
            startTime,
            endTime
        );
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> showtimeService.addShowtime(showtimeDTO));
        verify(showtimeRepository, never()).save(any());
    }

    @Test
    void addShowtime_NegativePrice_ThrowsException() {
        // Arrange
        ShowtimeDTO showtimeDTO = new ShowtimeDTO(
            null,
            -5.0, // Negative price
            1L,
            "Theater 1",
            startTime,
            endTime
        );
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> showtimeService.addShowtime(showtimeDTO));
        verify(showtimeRepository, never()).save(any());
    }

    @Test
    void addShowtime_NullTheater_ThrowsException() {
        // Arrange
        ShowtimeDTO showtimeDTO = new ShowtimeDTO(
            null,
            12.50,
            1L,
            null, // Null theater
            startTime,
            endTime
        );
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> showtimeService.addShowtime(showtimeDTO));
        verify(showtimeRepository, never()).save(any());
    }

    @Test
    void addShowtime_EmptyTheater_ThrowsException() {
        // Arrange
        ShowtimeDTO showtimeDTO = new ShowtimeDTO(
            null,
            12.50,
            1L,
            "", // Empty theater
            startTime,
            endTime
        );
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> showtimeService.addShowtime(showtimeDTO));
        verify(showtimeRepository, never()).save(any());
    }

    @Test
    void addShowtime_NullStartTime_ThrowsException() {
        // Arrange
        ShowtimeDTO showtimeDTO = new ShowtimeDTO(
            null,
            12.50,
            1L,
            "Theater 1",
            null, // Null start time
            endTime
        );
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> showtimeService.addShowtime(showtimeDTO));
        verify(showtimeRepository, never()).save(any());
    }

    @Test
    void addShowtime_NullEndTime_ThrowsException() {
        // Arrange
        ShowtimeDTO showtimeDTO = new ShowtimeDTO(
            null,
            12.50,
            1L,
            "Theater 1",
            startTime,
            null // Null end time
        );
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> showtimeService.addShowtime(showtimeDTO));
        verify(showtimeRepository, never()).save(any());
    }

    @Test
    void addShowtime_EndTimeBeforeStartTime_ThrowsException() {
        // Arrange
        LocalDateTime earlierEndTime = startTime.minusHours(1);
        ShowtimeDTO showtimeDTO = new ShowtimeDTO(
            null,
            12.50,
            1L,
            "Theater 1",
            startTime,
            earlierEndTime // End time before start time
        );
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> showtimeService.addShowtime(showtimeDTO));
        verify(showtimeRepository, never()).save(any());
    }

    @Test
    void addShowtime_EndTimeEqualsStartTime_ThrowsException() {
        // Arrange
        ShowtimeDTO showtimeDTO = new ShowtimeDTO(
            null,
            12.50,
            1L,
            "Theater 1",
            startTime,
            startTime // Equal times
        );
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> showtimeService.addShowtime(showtimeDTO));
        verify(showtimeRepository, never()).save(any());
    }

    @Test
    void updateShowtime_InvalidData_ThrowsException() {
        // Arrange
        ShowtimeDTO showtimeDTO = new ShowtimeDTO(
            1L,
            -10.0, // Negative price
            1L,
            "Theater 1",
            startTime,
            endTime
        );
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> showtimeService.updateShowtime(1L, showtimeDTO));
        verify(showtimeRepository, never()).save(any());
    }
}
