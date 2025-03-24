package com.att.tdp.popcorn_palace.controller.validation;

import com.att.tdp.popcorn_palace.controller.ShowtimeController;
import com.att.tdp.popcorn_palace.dto.ShowtimeDTO;
import com.att.tdp.popcorn_palace.service.ShowtimeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ShowtimeController.class)
public class ShowtimeControllerValidationTest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @MockBean
    private ShowtimeService showtimeService;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    public void addShowtime_NullMovieId_ReturnsBadRequest() throws Exception {
        ShowtimeDTO showtimeDTO = new ShowtimeDTO();
        showtimeDTO.setMovieId(null);
        showtimeDTO.setTheater("Theater 1");
        showtimeDTO.setPrice(12.50);
        showtimeDTO.setStartTime(LocalDateTime.now().plusDays(1));
        showtimeDTO.setEndTime(LocalDateTime.now().plusDays(1).plusHours(2));

        // Simulate service layer throwing IllegalArgumentException
        doThrow(new IllegalArgumentException("Movie ID is required for a showtime"))
            .when(showtimeService).addShowtime(any(ShowtimeDTO.class));

        mockMvc.perform(post("/showtimes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(showtimeDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void addShowtime_EmptyTheater_ReturnsBadRequest() throws Exception {
        ShowtimeDTO showtimeDTO = new ShowtimeDTO();
        showtimeDTO.setMovieId(1L);
        showtimeDTO.setTheater("");
        showtimeDTO.setPrice(12.50);
        showtimeDTO.setStartTime(LocalDateTime.now().plusDays(1));
        showtimeDTO.setEndTime(LocalDateTime.now().plusDays(1).plusHours(2));

        // Simulate service layer throwing IllegalArgumentException
        doThrow(new IllegalArgumentException("Theater name is required for a showtime"))
            .when(showtimeService).addShowtime(any(ShowtimeDTO.class));

        mockMvc.perform(post("/showtimes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(showtimeDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void addShowtime_NegativePrice_ReturnsBadRequest() throws Exception {
        ShowtimeDTO showtimeDTO = new ShowtimeDTO();
        showtimeDTO.setMovieId(1L);
        showtimeDTO.setTheater("Theater 1");
        showtimeDTO.setPrice(-5.0);
        showtimeDTO.setStartTime(LocalDateTime.now().plusDays(1));
        showtimeDTO.setEndTime(LocalDateTime.now().plusDays(1).plusHours(2));

        // Simulate service layer throwing IllegalArgumentException
        doThrow(new IllegalArgumentException("Showtime price must be greater than 0.0"))
            .when(showtimeService).addShowtime(any(ShowtimeDTO.class));

        mockMvc.perform(post("/showtimes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(showtimeDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void addShowtime_NullStartTime_ReturnsBadRequest() throws Exception {
        ShowtimeDTO showtimeDTO = new ShowtimeDTO();
        showtimeDTO.setMovieId(1L);
        showtimeDTO.setTheater("Theater 1");
        showtimeDTO.setPrice(12.50);
        showtimeDTO.setStartTime(null);
        showtimeDTO.setEndTime(LocalDateTime.now().plusDays(1).plusHours(2));

        // Simulate service layer throwing IllegalArgumentException
        doThrow(new IllegalArgumentException("Start time is required for a showtime"))
            .when(showtimeService).addShowtime(any(ShowtimeDTO.class));

        mockMvc.perform(post("/showtimes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(showtimeDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void addShowtime_NullEndTime_ReturnsBadRequest() throws Exception {
        ShowtimeDTO showtimeDTO = new ShowtimeDTO();
        showtimeDTO.setMovieId(1L);
        showtimeDTO.setTheater("Theater 1");
        showtimeDTO.setPrice(12.50);
        showtimeDTO.setStartTime(LocalDateTime.now().plusDays(1));
        showtimeDTO.setEndTime(null);

        // Simulate service layer throwing IllegalArgumentException
        doThrow(new IllegalArgumentException("End time is required for a showtime"))
            .when(showtimeService).addShowtime(any(ShowtimeDTO.class));

        mockMvc.perform(post("/showtimes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(showtimeDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void updateShowtime_InvalidData_ReturnsBadRequest() throws Exception {
        ShowtimeDTO showtimeDTO = new ShowtimeDTO();
        showtimeDTO.setMovieId(1L);
        showtimeDTO.setTheater("");  // Empty theater name
        showtimeDTO.setPrice(12.50);
        showtimeDTO.setStartTime(LocalDateTime.now().plusDays(1));
        showtimeDTO.setEndTime(LocalDateTime.now().plusDays(1).plusHours(2));

        // Simulate service layer throwing IllegalArgumentException
        doThrow(new IllegalArgumentException("Theater name is required for a showtime"))
            .when(showtimeService).updateShowtime(anyLong(), any(ShowtimeDTO.class));

        mockMvc.perform(put("/showtimes/update/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(showtimeDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void addShowtime_ValidData_ReturnsOk() throws Exception {
        ShowtimeDTO showtimeDTO = new ShowtimeDTO();
        showtimeDTO.setMovieId(1L);
        showtimeDTO.setTheater("Theater 1");
        showtimeDTO.setPrice(12.50);
        showtimeDTO.setStartTime(LocalDateTime.now().plusDays(1));
        showtimeDTO.setEndTime(LocalDateTime.now().plusDays(1).plusHours(2));

        when(showtimeService.addShowtime(any(ShowtimeDTO.class))).thenReturn(showtimeDTO);

        mockMvc.perform(post("/showtimes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(showtimeDTO)))
                .andExpect(status().isOk());
    }
}
