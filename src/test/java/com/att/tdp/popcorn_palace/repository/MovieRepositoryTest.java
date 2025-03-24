package com.att.tdp.popcorn_palace.repository;

import com.att.tdp.popcorn_palace.entity.Movie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class MovieRepositoryTest {

    @Autowired
    private MovieRepository movieRepository;

    @Test
    public void testSaveMovie() {
        // Create a new movie
        Movie movie = new Movie();
        movie.setTitle("Test Movie");
        movie.setGenre("Action");
        movie.setDuration(120);
        movie.setRating(8.5);
        movie.setReleaseYear(2023);

        // Save the movie
        Movie savedMovie = movieRepository.save(movie);

        // Verify the movie was saved
        assertNotNull(savedMovie.getId());
        assertEquals("Test Movie", savedMovie.getTitle());
    }

    @Test
    public void testFindByTitle() {
        // Create and save a movie
        Movie movie = new Movie();
        movie.setTitle("Inception");
        movie.setGenre("Sci-Fi");
        movie.setDuration(148);
        movie.setRating(8.8);
        movie.setReleaseYear(2010);
        movieRepository.save(movie);

        // Find movie by title
        Optional<Movie> foundMovie = movieRepository.findByTitle("Inception");

        // Verify the movie was found
        assertTrue(foundMovie.isPresent());
        assertEquals("Inception", foundMovie.get().getTitle());
    }

    @Test
    public void testFindByTitle_NotFound() {
        // Try to find a non-existent movie
        Optional<Movie> notFoundMovie = movieRepository.findByTitle("Non-existent Movie");

        // Verify no movie was found
        assertFalse(notFoundMovie.isPresent());
    }

    @Test
    public void testDeleteByTitle() {
        // Create and save a movie
        Movie movie = new Movie();
        movie.setTitle("Movie to Delete");
        movie.setGenre("Drama");
        movie.setDuration(100);
        movie.setRating(7.5);
        movie.setReleaseYear(2022);
        movieRepository.save(movie);

        // Verify movie exists
        assertTrue(movieRepository.findByTitle("Movie to Delete").isPresent());

        // Delete the movie
        movieRepository.deleteByTitle("Movie to Delete");

        // Verify movie was deleted
        assertFalse(movieRepository.findByTitle("Movie to Delete").isPresent());
    }

    @Test
    public void testFindAll() {
        // Create and save two movies
        Movie movie1 = new Movie();
        movie1.setTitle("Movie 1");
        movie1.setGenre("Action");
        movie1.setDuration(110);
        movie1.setRating(7.0);
        movie1.setReleaseYear(2020);
        movieRepository.save(movie1);

        Movie movie2 = new Movie();
        movie2.setTitle("Movie 2");
        movie2.setGenre("Comedy");
        movie2.setDuration(95);
        movie2.setRating(8.0);
        movie2.setReleaseYear(2021);
        movieRepository.save(movie2);

        // Find all movies
        List<Movie> movies = movieRepository.findAll();

        // Verify both movies were found
        assertTrue(movies.size() >= 2);
        assertTrue(movies.stream().anyMatch(m -> m.getTitle().equals("Movie 1")));
        assertTrue(movies.stream().anyMatch(m -> m.getTitle().equals("Movie 2")));
    }
}
