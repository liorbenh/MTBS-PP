package com.att.tdp.popcorn_palace.service.validation;

import com.att.tdp.popcorn_palace.dto.MovieDTO;
import com.att.tdp.popcorn_palace.repository.MovieRepository;
import com.att.tdp.popcorn_palace.repository.ShowtimeRepository;
import com.att.tdp.popcorn_palace.service.MovieService;
import com.att.tdp.popcorn_palace.service.ShowtimeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MovieServiceValidationTest {

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private ShowtimeRepository showtimeRepository;

    @Mock
    private ShowtimeService showtimeService;

    @InjectMocks
    private MovieService movieService;

    private MovieDTO validMovieDTO;

    @BeforeEach
    void setUp() {
        validMovieDTO = new MovieDTO(null, "Test Movie", "Action", 120, 8.5, 2022);
    }

    @Test
    void addMovie_NullTitle_ThrowsException() {
        // Arrange
        MovieDTO movieDTO = new MovieDTO(null, null, "Action", 120, 8.5, 2022);
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> movieService.addMovie(movieDTO));
        verify(movieRepository, never()).save(any());
    }

    @Test
    void addMovie_EmptyTitle_ThrowsException() {
        // Arrange
        MovieDTO movieDTO = new MovieDTO(null, "", "Action", 120, 8.5, 2022);
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> movieService.addMovie(movieDTO));
        verify(movieRepository, never()).save(any());
    }

    @Test
    void addMovie_NullGenre_ThrowsException() {
        // Arrange
        MovieDTO movieDTO = new MovieDTO(null, "Test Movie", null, 120, 8.5, 2022);
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> movieService.addMovie(movieDTO));
        verify(movieRepository, never()).save(any());
    }

    @Test
    void addMovie_EmptyGenre_ThrowsException() {
        // Arrange
        MovieDTO movieDTO = new MovieDTO(null, "Test Movie", "", 120, 8.5, 2022);
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> movieService.addMovie(movieDTO));
        verify(movieRepository, never()).save(any());
    }

    @Test
    void addMovie_ZeroDuration_ThrowsException() {
        // Arrange
        MovieDTO movieDTO = new MovieDTO(null, "Test Movie", "Action", 0, 8.5, 2022);
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> movieService.addMovie(movieDTO));
        verify(movieRepository, never()).save(any());
    }

    @Test
    void addMovie_NegativeDuration_ThrowsException() {
        // Arrange
        MovieDTO movieDTO = new MovieDTO(null, "Test Movie", "Action", -10, 8.5, 2022);
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> movieService.addMovie(movieDTO));
        verify(movieRepository, never()).save(any());
    }

    @Test
    void addMovie_NegativeRating_ThrowsException() {
        // Arrange
        MovieDTO movieDTO = new MovieDTO(null, "Test Movie", "Action", 120, -1.0, 2022);
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> movieService.addMovie(movieDTO));
        verify(movieRepository, never()).save(any());
    }

    @Test
    void addMovie_RatingAboveTen_ThrowsException() {
        // Arrange
        MovieDTO movieDTO = new MovieDTO(null, "Test Movie", "Action", 120, 11.0, 2022);
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> movieService.addMovie(movieDTO));
        verify(movieRepository, never()).save(any());
    }

    @Test
    void addMovie_ReleaseYearTooEarly_ThrowsException() {
        // Arrange
        MovieDTO movieDTO = new MovieDTO(null, "Test Movie", "Action", 120, 8.5, 1879);
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> movieService.addMovie(movieDTO));
        verify(movieRepository, never()).save(any());
    }
}
