package com.att.tdp.popcorn_palace.controller;

import com.att.tdp.popcorn_palace.dto.BookingDTO;
import com.att.tdp.popcorn_palace.dto.BookingResponseDTO;
import com.att.tdp.popcorn_palace.exception.ResourceNotFoundException;
import com.att.tdp.popcorn_palace.exception.SeatAlreadyBookedException;
import com.att.tdp.popcorn_palace.service.BookingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
public class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookingService bookingService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testBookTicket_Success() throws Exception {
        // Given
        UUID userId = UUID.randomUUID();
        UUID bookingId = UUID.randomUUID();
        
        BookingDTO bookingDTO = new BookingDTO();
        bookingDTO.setShowtimeId(1L);
        bookingDTO.setSeatNumber(10);
        bookingDTO.setUserId(userId);
        
        BookingResponseDTO responseDTO = new BookingResponseDTO(bookingId);
        
        when(bookingService.bookTicket(any(BookingDTO.class))).thenReturn(responseDTO);

        // When & Then
        mockMvc.perform(post("/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bookingDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookingId").value(bookingId.toString()));
    }

    @Test
    public void testBookTicket_SeatAlreadyBooked() throws Exception {
        // Given
        UUID userId = UUID.randomUUID();
        
        BookingDTO bookingDTO = new BookingDTO();
        bookingDTO.setShowtimeId(1L);
        bookingDTO.setSeatNumber(10);
        bookingDTO.setUserId(userId);
        
        when(bookingService.bookTicket(any(BookingDTO.class)))
                .thenThrow(new SeatAlreadyBookedException("Seat already booked"));

        // When & Then
        mockMvc.perform(post("/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bookingDTO)))
                .andExpect(status().isConflict());
    }

    @Test
    public void testBookTicket_ShowtimeNotFound() throws Exception {
        // Given
        UUID userId = UUID.randomUUID();
        
        BookingDTO bookingDTO = new BookingDTO();
        bookingDTO.setShowtimeId(999L);
        bookingDTO.setSeatNumber(10);
        bookingDTO.setUserId(userId);
        
        when(bookingService.bookTicket(any(BookingDTO.class)))
                .thenThrow(new ResourceNotFoundException("Showtime not found"));

        // When & Then
        mockMvc.perform(post("/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bookingDTO)))
                .andExpect(status().isNotFound());
    }
}
