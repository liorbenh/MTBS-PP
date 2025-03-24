package com.att.tdp.popcorn_palace.service;

import com.att.tdp.popcorn_palace.dto.MovieDTO;
import com.att.tdp.popcorn_palace.entity.Movie;
import com.att.tdp.popcorn_palace.exception.ResourceAlreadyExistsException;
import com.att.tdp.popcorn_palace.exception.ResourceNotFoundException;
import com.att.tdp.popcorn_palace.repository.MovieRepository;
import com.att.tdp.popcorn_palace.repository.ShowtimeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MovieService {

    private final MovieRepository movieRepository;
    private final ShowtimeRepository showtimeRepository;
    private final ShowtimeService showtimeService;

    @Autowired
    public MovieService(MovieRepository movieRepository, ShowtimeRepository showtimeRepository, ShowtimeService showtimeService) {
        this.movieRepository = movieRepository;
        this.showtimeRepository = showtimeRepository;
        this.showtimeService = showtimeService;
    }

    public List<MovieDTO> getAllMovies() {
        return movieRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public MovieDTO getMovieByTitle(String title) {
        Movie movie = movieRepository.findByTitle(title)
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found with title: " + title));
        return convertToDTO(movie);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public MovieDTO addMovie(MovieDTO movieDto) {
        validateMovieData(movieDto);
        // Check if movie with this title already exists
        if (movieRepository.findByTitle(movieDto.getTitle()).isPresent()) {
            throw new ResourceAlreadyExistsException("Movie with title " + movieDto.getTitle() + " already exists.");
        }
        
        Movie movie = convertToEntity(movieDto);
        Movie savedMovie = movieRepository.save(movie);
        return convertToDTO(savedMovie);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public MovieDTO updateMovie(String movieTitle, MovieDTO movieDto) {
        validateMovieData(movieDto);
        Movie movie = movieRepository.findByTitle(movieTitle)
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found with title: " + movieTitle));
        
        // Update the movie properties
        movie.setTitle(movieDto.getTitle());
        movie.setGenre(movieDto.getGenre());
        movie.setDuration(movieDto.getDuration());
        movie.setRating(movieDto.getRating());
        movie.setReleaseYear(movieDto.getReleaseYear());
        
        Movie updatedMovie = movieRepository.save(movie);
        return convertToDTO(updatedMovie);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void deleteMovie(String movieTitle) {
        Movie movie = movieRepository.findByTitle(movieTitle)
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found with title: " + movieTitle));
        
        // Per data flow: Delete associated showtimes when a movie is deleted
        List<Long> showtimeIds = showtimeRepository.findByMovieId(movie.getId())
                .stream()
                .map(showtime -> showtime.getId())
                .collect(Collectors.toList());
        
        // Delete each showtime (this will cascade to delete associated bookings and show seats)
        for (Long showtimeId : showtimeIds) {
            showtimeService.deleteShowtime(showtimeId);
        }
        
        // Now delete the movie itself
        movieRepository.delete(movie);
    }

    // Helper methods for DTO to Entity conversion
    private MovieDTO convertToDTO(Movie movie) {
        return new MovieDTO(
                movie.getId(),
                movie.getTitle(),
                movie.getGenre(),
                movie.getDuration(),
                movie.getRating(),
                movie.getReleaseYear()
        );
    }

    private Movie convertToEntity(MovieDTO movieDTO) {
        return new Movie(
                movieDTO.getId(),
                movieDTO.getTitle(),
                movieDTO.getGenre(),
                movieDTO.getDuration(),
                movieDTO.getRating(),
                movieDTO.getReleaseYear()
        );
    }

    /**
     * Validates the movie data according to the data modeling requirements
     * @param movieDTO the movie data to validate
     * @throws IllegalArgumentException if validation fails
     */
    private void validateMovieData(MovieDTO movieDTO) {
        if (movieDTO == null) {
            throw new IllegalArgumentException("Movie data cannot be null");
        }
        
        if (movieDTO.getTitle() == null || movieDTO.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Movie title cannot be empty");
        }
        
        if (movieDTO.getGenre() == null || movieDTO.getGenre().trim().isEmpty()) {
            throw new IllegalArgumentException("Movie genre cannot be empty");
        }
        
        if (movieDTO.getDuration() == null || movieDTO.getDuration() <= 0) {
            throw new IllegalArgumentException("Movie duration must be greater than 0 minutes");
        }
        
        if (movieDTO.getRating() == null || movieDTO.getRating() < 0.0 || movieDTO.getRating() > 10.0) {
            throw new IllegalArgumentException("Movie rating must be between 0.0 and 10.0");
        }
        
        if (movieDTO.getReleaseYear() == null || movieDTO.getReleaseYear() <= 1880) {
            throw new IllegalArgumentException("Movie release year must be after 1880");
        }
    }
}
