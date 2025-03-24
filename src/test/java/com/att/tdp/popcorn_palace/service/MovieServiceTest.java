package com.att.tdp.popcorn_palace.service;

import com.att.tdp.popcorn_palace.dto.MovieDTO;
import com.att.tdp.popcorn_palace.entity.Movie;
import com.att.tdp.popcorn_palace.entity.Showtime;
import com.att.tdp.popcorn_palace.exception.ResourceAlreadyExistsException;
import com.att.tdp.popcorn_palace.exception.ResourceNotFoundException;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MovieServiceTest {

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private ShowtimeRepository showtimeRepository;

    @Mock
    private ShowtimeService showtimeService;

    @InjectMocks
    private MovieService movieService;

    private Movie movie;
    private MovieDTO movieDTO;
    private List<Movie> movies;

    @BeforeEach
    void setUp() {
        // Initialize test data
        movie = new Movie(1L, "Inception", "Science Fiction", 148, 8.8, 2010);
        movieDTO = new MovieDTO(1L, "Inception", "Science Fiction", 148, 8.8, 2010);
        
        movies = new ArrayList<>();
        movies.add(movie);
        movies.add(new Movie(2L, "The Dark Knight", "Action", 152, 9.0, 2008));
    }

    @Test
    void getAllMovies_Success() {
        // Arrange
        when(movieRepository.findAll()).thenReturn(movies);
        
        // Act
        List<MovieDTO> result = movieService.getAllMovies();
        
        // Assert
        assertEquals(2, result.size());
        assertEquals("Inception", result.get(0).getTitle());
        assertEquals("The Dark Knight", result.get(1).getTitle());
    }

    @Test
    void getMovieByTitle_Success() {
        // Arrange
        when(movieRepository.findByTitle("Inception")).thenReturn(Optional.of(movie));
        
        // Act
        MovieDTO result = movieService.getMovieByTitle("Inception");
        
        // Assert
        assertEquals(1L, result.getId());
        assertEquals("Inception", result.getTitle());
        assertEquals("Science Fiction", result.getGenre());
    }

    @Test
    void getMovieByTitle_NotFound() {
        // Arrange
        when(movieRepository.findByTitle("Non-existent")).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(ResourceNotFoundException.class, 
                     () -> movieService.getMovieByTitle("Non-existent"));
    }

    @Test
    void addMovie_Success() {
        // Arrange
        MovieDTO newMovieDTO = new MovieDTO(null, "New Movie", "Drama", 120, 7.5, 2020);
        Movie newMovie = new Movie(3L, "New Movie", "Drama", 120, 7.5, 2020);
        
        when(movieRepository.findByTitle("New Movie")).thenReturn(Optional.empty());
        when(movieRepository.save(any(Movie.class))).thenReturn(newMovie);
        
        // Act
        MovieDTO result = movieService.addMovie(newMovieDTO);
        
        // Assert
        assertEquals(3L, result.getId());
        assertEquals("New Movie", result.getTitle());
        verify(movieRepository).save(any(Movie.class));
    }

    @Test
    void addMovie_AlreadyExists() {
        // Arrange
        when(movieRepository.findByTitle("Inception")).thenReturn(Optional.of(movie));
        
        // Act & Assert
        assertThrows(ResourceAlreadyExistsException.class, 
                     () -> movieService.addMovie(movieDTO));
        verify(movieRepository, never()).save(any(Movie.class));
    }

    @Test
    void updateMovie_Success() {
        // Arrange
        MovieDTO updatedDTO = new MovieDTO(1L, "Inception Updated", "Thriller", 150, 9.0, 2010);
        Movie updatedMovie = new Movie(1L, "Inception Updated", "Thriller", 150, 9.0, 2010);
        
        when(movieRepository.findByTitle("Inception")).thenReturn(Optional.of(movie));
        when(movieRepository.save(any(Movie.class))).thenReturn(updatedMovie);
        
        // Act
        MovieDTO result = movieService.updateMovie("Inception", updatedDTO);
        
        // Assert
        assertEquals("Inception Updated", result.getTitle());
        assertEquals("Thriller", result.getGenre());
        assertEquals(150, result.getDuration());
        verify(movieRepository).save(any(Movie.class));
    }

    @Test
    void updateMovie_NotFound() {
        // Arrange
        when(movieRepository.findByTitle("Non-existent")).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(ResourceNotFoundException.class, 
                     () -> movieService.updateMovie("Non-existent", movieDTO));
        verify(movieRepository, never()).save(any(Movie.class));
    }

    @Test
    void deleteMovie_Success() {
        // Arrange
        when(movieRepository.findByTitle("Inception")).thenReturn(Optional.of(movie));
        
        // Create some showtimes related to this movie
        List<Showtime> relatedShowtimes = Arrays.asList(
            new Showtime(1L, 12.50, 1L, "Theater 1", LocalDateTime.now(), LocalDateTime.now().plusHours(2)),
            new Showtime(2L, 15.00, 1L, "Theater 2", LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(2))
        );
        
        when(showtimeRepository.findByMovieId(1L)).thenReturn(relatedShowtimes);
        
        // Act
        movieService.deleteMovie("Inception");
        
        // Assert - verify showtimes are deleted first according to the data flow
        verify(showtimeService).deleteShowtime(1L);
        verify(showtimeService).deleteShowtime(2L);
        verify(movieRepository).delete(movie);
    }

    @Test
    void deleteMovie_NotFound() {
        // Arrange
        when(movieRepository.findByTitle("Non-existent")).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(ResourceNotFoundException.class, 
                     () -> movieService.deleteMovie("Non-existent"));
    }
}
