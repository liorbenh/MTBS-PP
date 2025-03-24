package com.att.tdp.popcorn_palace.controller;

import com.att.tdp.popcorn_palace.dto.MovieDTO;
import com.att.tdp.popcorn_palace.exception.ResourceAlreadyExistsException;
import com.att.tdp.popcorn_palace.exception.ResourceNotFoundException;
import com.att.tdp.popcorn_palace.service.MovieService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MovieController.class)
public class MovieControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MovieService movieService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testGetAllMovies() throws Exception {
        // Given
        MovieDTO movie1 = new MovieDTO(1L, "Inception", "Sci-Fi", 148, 8.8, 2010);
        MovieDTO movie2 = new MovieDTO(2L, "The Dark Knight", "Action", 152, 9.0, 2008);
        List<MovieDTO> movies = Arrays.asList(movie1, movie2);

        when(movieService.getAllMovies()).thenReturn(movies);

        // When & Then
        mockMvc.perform(get("/movies/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Inception"))
                .andExpect(jsonPath("$[1].title").value("The Dark Knight"));
    }

    @Test
    public void testAddMovie() throws Exception {
        // Given
        MovieDTO movieToAdd = new MovieDTO(null, "Interstellar", "Sci-Fi", 169, 8.6, 2014);
        MovieDTO addedMovie = new MovieDTO(3L, "Interstellar", "Sci-Fi", 169, 8.6, 2014);

        when(movieService.addMovie(any(MovieDTO.class))).thenReturn(addedMovie);

        // When & Then
        mockMvc.perform(post("/movies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(movieToAdd)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.title").value("Interstellar"));
    }

    @Test
    public void testAddMovie_AlreadyExists() throws Exception {
        // Given
        MovieDTO movieToAdd = new MovieDTO(null, "Inception", "Sci-Fi", 148, 8.8, 2010);

        when(movieService.addMovie(any(MovieDTO.class)))
                .thenThrow(new ResourceAlreadyExistsException("Movie with title Inception already exists"));

        // When & Then
        mockMvc.perform(post("/movies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(movieToAdd)))
                .andExpect(status().isConflict());
    }

    @Test
    public void testUpdateMovie() throws Exception {
        // Given
        MovieDTO movieToUpdate = new MovieDTO(1L, "Inception Updated", "Sci-Fi", 148, 8.9, 2010);
        
        when(movieService.updateMovie(eq("Inception"), any(MovieDTO.class))).thenReturn(movieToUpdate);

        // When & Then
        mockMvc.perform(put("/movies/update/Inception")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(movieToUpdate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Inception Updated"));
    }

    @Test
    public void testUpdateMovie_NotFound() throws Exception {
        // Given
        MovieDTO movieToUpdate = new MovieDTO(999L, "Non-existent Movie", "Drama", 120, 7.0, 2020);
        
        when(movieService.updateMovie(eq("Non-existent"), any(MovieDTO.class)))
                .thenThrow(new ResourceNotFoundException("Movie not found"));

        // When & Then
        mockMvc.perform(put("/movies/update/Non-existent")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(movieToUpdate)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testDeleteMovie() throws Exception {
        // Given
        doNothing().when(movieService).deleteMovie("Inception");

        // When & Then
        mockMvc.perform(delete("/movies/Inception"))
                .andExpect(status().isOk());
                
        verify(movieService, times(1)).deleteMovie("Inception");
    }

    @Test
    public void testDeleteMovie_NotFound() throws Exception {
        // Given
        doThrow(new ResourceNotFoundException("Movie not found with title: Non-existent"))
            .when(movieService).deleteMovie("Non-existent");

        // When & Then
        mockMvc.perform(delete("/movies/Non-existent"))
                .andExpect(status().isNotFound());
    }
}
