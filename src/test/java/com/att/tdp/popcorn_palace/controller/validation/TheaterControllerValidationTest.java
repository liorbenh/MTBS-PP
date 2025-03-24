package com.att.tdp.popcorn_palace.controller.validation;

import com.att.tdp.popcorn_palace.controller.TheaterController;
import com.att.tdp.popcorn_palace.dto.TheaterDTO;
import com.att.tdp.popcorn_palace.service.TheaterService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TheaterController.class)
public class TheaterControllerValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TheaterService theaterService;

    @Test
    public void createTheater_EmptyName_ReturnsBadRequest() throws Exception {
        TheaterDTO theaterDTO = new TheaterDTO();
        theaterDTO.setName("");
        theaterDTO.setNumberOfSeats(100);

        // Simulate service layer throwing IllegalArgumentException for empty name
        doThrow(new IllegalArgumentException("Theater name cannot be empty"))
            .when(theaterService).addTheater(any(TheaterDTO.class));

        mockMvc.perform(post("/theaters")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(theaterDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void createTheater_NullName_ReturnsBadRequest() throws Exception {
        TheaterDTO theaterDTO = new TheaterDTO();
        theaterDTO.setName(null);
        theaterDTO.setNumberOfSeats(100);

        // Simulate service layer throwing IllegalArgumentException for null name
        doThrow(new IllegalArgumentException("Theater name cannot be empty"))
            .when(theaterService).addTheater(any(TheaterDTO.class));

        mockMvc.perform(post("/theaters")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(theaterDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void createTheater_NegativeSeats_ReturnsBadRequest() throws Exception {
        TheaterDTO theaterDTO = new TheaterDTO();
        theaterDTO.setName("Test Theater");
        theaterDTO.setNumberOfSeats(-10);

        // Simulate service layer throwing IllegalArgumentException for negative seats
        doThrow(new IllegalArgumentException("Number of seats must be greater than 0"))
            .when(theaterService).addTheater(any(TheaterDTO.class));

        mockMvc.perform(post("/theaters")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(theaterDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void createTheater_ZeroSeats_ReturnsBadRequest() throws Exception {
        TheaterDTO theaterDTO = new TheaterDTO();
        theaterDTO.setName("Test Theater");
        theaterDTO.setNumberOfSeats(0);

        // Simulate service layer throwing IllegalArgumentException for zero seats
        doThrow(new IllegalArgumentException("Number of seats must be greater than 0"))
            .when(theaterService).addTheater(any(TheaterDTO.class));

        mockMvc.perform(post("/theaters")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(theaterDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void createTheater_ValidData_ReturnsCreated() throws Exception {
        TheaterDTO theaterDTO = new TheaterDTO();
        theaterDTO.setName("Test Theater");
        theaterDTO.setNumberOfSeats(100);

        when(theaterService.addTheater(any(TheaterDTO.class))).thenReturn(theaterDTO);

        mockMvc.perform(post("/theaters")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(theaterDTO)))
                .andExpect(status().isCreated());
    }
}
