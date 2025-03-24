package com.att.tdp.popcorn_palace.controller.validation;

import com.att.tdp.popcorn_palace.controller.MovieController;
import com.att.tdp.popcorn_palace.dto.MovieDTO;
import com.att.tdp.popcorn_palace.service.MovieService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MovieController.class)
public class MovieControllerValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MovieService movieService;

    @Test
    public void addMovie_EmptyTitle_ReturnsBadRequest() throws Exception {
        // Create movie with empty title
        MovieDTO movieDTO = new MovieDTO();
        movieDTO.setTitle("");
        movieDTO.setGenre("Action");
        movieDTO.setDuration(120);
        movieDTO.setRating(8.5);
        movieDTO.setReleaseYear(2022);

        // Simulate service layer throwing IllegalArgumentException for empty title
        doThrow(new IllegalArgumentException("Movie title cannot be empty"))
            .when(movieService).addMovie(any(MovieDTO.class));

        mockMvc.perform(post("/movies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(movieDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void addMovie_EmptyGenre_ReturnsBadRequest() throws Exception {
        // Create movie with empty genre
        MovieDTO movieDTO = new MovieDTO();
        movieDTO.setTitle("Test Movie");
        movieDTO.setGenre("");
        movieDTO.setDuration(120);
        movieDTO.setRating(8.5);
        movieDTO.setReleaseYear(2022);

        // Simulate service layer throwing IllegalArgumentException for empty genre
        doThrow(new IllegalArgumentException("Movie genre cannot be empty"))
            .when(movieService).addMovie(any(MovieDTO.class));

        mockMvc.perform(post("/movies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(movieDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void addMovie_NegativeDuration_ReturnsBadRequest() throws Exception {
        // Create movie with negative duration
        MovieDTO movieDTO = new MovieDTO();
        movieDTO.setTitle("Test Movie");
        movieDTO.setGenre("Action");
        movieDTO.setDuration(-10);
        movieDTO.setRating(8.5);
        movieDTO.setReleaseYear(2022);

        // Simulate service layer throwing IllegalArgumentException for negative duration
        doThrow(new IllegalArgumentException("Movie duration must be greater than 0 minutes"))
            .when(movieService).addMovie(any(MovieDTO.class));

        mockMvc.perform(post("/movies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(movieDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void addMovie_RatingTooHigh_ReturnsBadRequest() throws Exception {
        // Create movie with rating > 10
        MovieDTO movieDTO = new MovieDTO();
        movieDTO.setTitle("Test Movie");
        movieDTO.setGenre("Action");
        movieDTO.setDuration(120);
        movieDTO.setRating(11.0);
        movieDTO.setReleaseYear(2022);

        // Simulate service layer throwing IllegalArgumentException for invalid rating
        doThrow(new IllegalArgumentException("Movie rating must be between 0.0 and 10.0"))
            .when(movieService).addMovie(any(MovieDTO.class));

        mockMvc.perform(post("/movies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(movieDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void addMovie_ReleaseYearTooEarly_ReturnsBadRequest() throws Exception {
        // Create movie with release year before 1880
        MovieDTO movieDTO = new MovieDTO();
        movieDTO.setTitle("Test Movie");
        movieDTO.setGenre("Action");
        movieDTO.setDuration(120);
        movieDTO.setRating(8.5);
        movieDTO.setReleaseYear(1879);

        // Simulate service layer throwing IllegalArgumentException for invalid year
        doThrow(new IllegalArgumentException("Movie release year must be after 1880"))
            .when(movieService).addMovie(any(MovieDTO.class));

        mockMvc.perform(post("/movies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(movieDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void updateMovie_InvalidData_ReturnsBadRequest() throws Exception {
        // Create movie with invalid data
        MovieDTO movieDTO = new MovieDTO();
        movieDTO.setTitle("New Title");
        movieDTO.setGenre("");  // Empty genre
        movieDTO.setDuration(120);
        movieDTO.setRating(8.5);
        movieDTO.setReleaseYear(2022);

        // Simulate service layer throwing IllegalArgumentException for empty genre
        doThrow(new IllegalArgumentException("Movie genre cannot be empty"))
            .when(movieService).updateMovie(anyString(), any(MovieDTO.class));

        mockMvc.perform(put("/movies/update/Original Title")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(movieDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void addMovie_ValidData_ReturnsOk() throws Exception {
        // Create valid movie data
        MovieDTO movieDTO = new MovieDTO();
        movieDTO.setTitle("Test Movie");
        movieDTO.setGenre("Action");
        movieDTO.setDuration(120);
        movieDTO.setRating(8.5);
        movieDTO.setReleaseYear(2022);

        when(movieService.addMovie(any(MovieDTO.class))).thenReturn(movieDTO);

        mockMvc.perform(post("/movies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(movieDTO)))
                .andExpect(status().isOk());
    }
}
