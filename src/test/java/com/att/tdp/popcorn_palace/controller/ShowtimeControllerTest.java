package com.att.tdp.popcorn_palace.controller;

import com.att.tdp.popcorn_palace.dto.ShowtimeDTO;
import com.att.tdp.popcorn_palace.exception.ResourceNotFoundException;
import com.att.tdp.popcorn_palace.exception.ShowtimeOverlapException;
import com.att.tdp.popcorn_palace.service.ShowtimeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.hasSize;

@WebMvcTest(ShowtimeController.class)
public class ShowtimeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ShowtimeService showtimeService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testGetShowtimeById() throws Exception {
        // Given
        LocalDateTime startTime = LocalDateTime.now();
        LocalDateTime endTime = startTime.plusHours(2);
        ShowtimeDTO showtime = new ShowtimeDTO(1L, 12.50, 1L, "Theater 1", startTime, endTime);
        
        when(showtimeService.getShowtimeById(1L)).thenReturn(showtime);

        // When & Then
        mockMvc.perform(get("/showtimes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.price").value(12.50))
                .andExpect(jsonPath("$.theater").value("Theater 1"));
    }

    @Test
    public void testGetShowtimeById_NotFound() throws Exception {
        // Given
        when(showtimeService.getShowtimeById(999L)).thenThrow(new ResourceNotFoundException("Showtime not found"));

        // When & Then
        mockMvc.perform(get("/showtimes/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testGetAllShowtimes() throws Exception {
        // Given
        LocalDateTime startTime = LocalDateTime.now();
        LocalDateTime endTime = startTime.plusHours(2);
        
        List<ShowtimeDTO> showtimes = Arrays.asList(
            new ShowtimeDTO(1L, 12.50, 1L, "Theater 1", startTime, endTime),
            new ShowtimeDTO(2L, 15.00, 2L, "Theater 2", startTime.plusHours(3), endTime.plusHours(3))
        );
        
        when(showtimeService.getAllShowtimes()).thenReturn(showtimes);
        
        // When & Then
        mockMvc.perform(get("/showtimes/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].price").value(12.50))
                .andExpect(jsonPath("$[0].theater").value("Theater 1"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].price").value(15.00))
                .andExpect(jsonPath("$[1].theater").value("Theater 2"));
        
        verify(showtimeService, times(1)).getAllShowtimes();
    }

    @Test
    public void testGetAllShowtimes_EmptyList() throws Exception {
        // Given
        when(showtimeService.getAllShowtimes()).thenReturn(new ArrayList<>());
        
        // When & Then
        mockMvc.perform(get("/showtimes/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
        
        verify(showtimeService, times(1)).getAllShowtimes();
    }

    @Test
    public void testAddShowtime() throws Exception {
        // Given
        LocalDateTime startTime = LocalDateTime.now();
        LocalDateTime endTime = startTime.plusHours(2);
        ShowtimeDTO showtimeToAdd = new ShowtimeDTO(null, 12.50, 1L, "Theater 1", startTime, endTime);
        ShowtimeDTO addedShowtime = new ShowtimeDTO(1L, 12.50, 1L, "Theater 1", startTime, endTime);
        
        when(showtimeService.addShowtime(any(ShowtimeDTO.class))).thenReturn(addedShowtime);

        // When & Then
        mockMvc.perform(post("/showtimes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(showtimeToAdd)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.price").value(12.50))
                .andExpect(jsonPath("$.theater").value("Theater 1"));
    }

    @Test
    public void testAddShowtime_Overlap() throws Exception {
        // Given
        LocalDateTime startTime = LocalDateTime.now();
        LocalDateTime endTime = startTime.plusHours(2);
        ShowtimeDTO showtimeToAdd = new ShowtimeDTO(null, 12.50, 1L, "Theater 1", startTime, endTime);
        
        when(showtimeService.addShowtime(any(ShowtimeDTO.class)))
                .thenThrow(new ShowtimeOverlapException("Overlapping showtime"));

        // When & Then
        mockMvc.perform(post("/showtimes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(showtimeToAdd)))
                .andExpect(status().isConflict());
    }

    @Test
    public void testAddShowtime_NotFound() throws Exception {
        // Given
        LocalDateTime startTime = LocalDateTime.now();
        LocalDateTime endTime = startTime.plusHours(2);
        ShowtimeDTO showtimeToAdd = new ShowtimeDTO(null, 12.50, 999L, "Theater 1", startTime, endTime);
        
        when(showtimeService.addShowtime(any(ShowtimeDTO.class)))
                .thenThrow(new ResourceNotFoundException("Movie with ID 999 not found"));

        // When & Then
        mockMvc.perform(post("/showtimes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(showtimeToAdd)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testUpdateShowtime() throws Exception {
        // Given
        LocalDateTime startTime = LocalDateTime.now();
        LocalDateTime endTime = startTime.plusHours(2);
        ShowtimeDTO showtimeToUpdate = new ShowtimeDTO(1L, 15.00, 1L, "Theater 1", startTime, endTime);
        
        when(showtimeService.updateShowtime(eq(1L), any(ShowtimeDTO.class))).thenReturn(showtimeToUpdate);

        // When & Then
        mockMvc.perform(put("/showtimes/update/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(showtimeToUpdate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.price").value(15.00));
    }
    
    @Test
    public void testUpdateShowtime_NotFound() throws Exception {
        // Given
        LocalDateTime startTime = LocalDateTime.now();
        LocalDateTime endTime = startTime.plusHours(2);
        ShowtimeDTO showtimeToUpdate = new ShowtimeDTO(999L, 15.00, 1L, "Theater 1", startTime, endTime);
        
        when(showtimeService.updateShowtime(eq(999L), any(ShowtimeDTO.class)))
                .thenThrow(new ResourceNotFoundException("Showtime with ID 999 not found"));

        // When & Then
        mockMvc.perform(put("/showtimes/update/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(showtimeToUpdate)))
                .andExpect(status().isNotFound());
    }
    
    @Test
    public void testUpdateShowtime_Overlap() throws Exception {
        // Given
        LocalDateTime startTime = LocalDateTime.now();
        LocalDateTime endTime = startTime.plusHours(2);
        ShowtimeDTO showtimeToUpdate = new ShowtimeDTO(1L, 15.00, 1L, "Theater 1", startTime, endTime);
        
        when(showtimeService.updateShowtime(eq(1L), any(ShowtimeDTO.class)))
                .thenThrow(new ShowtimeOverlapException("Updated showtime would overlap with existing showtime"));

        // When & Then
        mockMvc.perform(put("/showtimes/update/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(showtimeToUpdate)))
                .andExpect(status().isConflict());
    }

    @Test
    public void testDeleteShowtime() throws Exception {
        // Given
        doNothing().when(showtimeService).deleteShowtime(1L);

        // When & Then
        mockMvc.perform(delete("/showtimes/1"))
                .andExpect(status().isOk());
        
        verify(showtimeService, times(1)).deleteShowtime(1L);
    }

    @Test
    public void testDeleteShowtime_NotFound() throws Exception {
        // Given
        doThrow(new ResourceNotFoundException("Showtime not found"))
            .when(showtimeService).deleteShowtime(999L);

        // When & Then
        mockMvc.perform(delete("/showtimes/999"))
                .andExpect(status().isNotFound());
    }


}
