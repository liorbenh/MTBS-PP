package com.att.tdp.popcorn_palace.controller;

import com.att.tdp.popcorn_palace.dto.TheaterDTO;
import com.att.tdp.popcorn_palace.constants.TheaterConstants;
import com.att.tdp.popcorn_palace.exception.ResourceNotFoundException;
import com.att.tdp.popcorn_palace.exception.TheaterLimitExceededException;
import com.att.tdp.popcorn_palace.service.TheaterService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TheaterController.class)
public class TheaterControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TheaterService theaterService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testGetAllTheaters() throws Exception {
        // Setup mock response
        List<TheaterDTO> theaters = Arrays.asList(
                new TheaterDTO(1L, "Theater 1", 100),
                new TheaterDTO(2L, "Theater 2", 150)
        );
        when(theaterService.getAllTheaters()).thenReturn(theaters);

        // Perform GET request and verify
        mockMvc.perform(get("/theaters/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is("Theater 1")))
                .andExpect(jsonPath("$[0].numberOfSeats", is(100)))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].name", is("Theater 2")))
                .andExpect(jsonPath("$[1].numberOfSeats", is(150)));
    }

    @Test
    public void testGetTheaterById() throws Exception {
        // Setup mock response
        TheaterDTO theater = new TheaterDTO(1L, "Theater 1", 100);
        when(theaterService.getTheaterById(1L)).thenReturn(theater);

        // Perform GET request and verify
        mockMvc.perform(get("/theaters/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Theater 1")))
                .andExpect(jsonPath("$.numberOfSeats", is(100)));
    }

    @Test
    public void testGetTheaterById_NotFound() throws Exception {
        // Setup mock response
        when(theaterService.getTheaterById(999L)).thenThrow(new ResourceNotFoundException("Theater not found"));

        // Perform GET request and verify
        mockMvc.perform(get("/theaters/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testCreateTheater() throws Exception {
        // Setup request and mock response
        TheaterDTO requestTheater = new TheaterDTO(null, "New Theater", 200);
        TheaterDTO createdTheater = new TheaterDTO(1L, "New Theater", 200);
        
        when(theaterService.addTheater(any(TheaterDTO.class))).thenReturn(createdTheater);

        // Perform POST request and verify
        mockMvc.perform(post("/theaters")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestTheater)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("New Theater")))
                .andExpect(jsonPath("$.numberOfSeats", is(200)));
    }

    @Test
    public void testCreateTheater_WithDefaultSeats() throws Exception {
        // Setup request and mock response
        TheaterDTO requestTheater = new TheaterDTO(null, "Default Seats Theater");
        TheaterDTO createdTheater = new TheaterDTO(1L, "Default Seats Theater", TheaterConstants.DEFAULT_SEATS);
        
        when(theaterService.addTheater(any(TheaterDTO.class))).thenReturn(createdTheater);

        // Perform POST request and verify
        mockMvc.perform(post("/theaters")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestTheater)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Default Seats Theater")))
                .andExpect(jsonPath("$.numberOfSeats", is(TheaterConstants.DEFAULT_SEATS)));
    }

    @Test
    public void testCreateTheater_MaxLimitReached() throws Exception {
        // Setup request and mock response for failure
        TheaterDTO requestTheater = new TheaterDTO(null, "Overflow Theater", TheaterConstants.DEFAULT_SEATS);
        List<String> availableTheaters = Arrays.asList("Theater 1", "Theater 2");
        
        when(theaterService.addTheater(any(TheaterDTO.class))).thenThrow(
            new TheaterLimitExceededException("Maximum number of theaters reached", availableTheaters));

        // Perform POST request and verify
        mockMvc.perform(post("/theaters")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestTheater)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.message").exists());
    }
}
