package com.att.tdp.popcorn_palace.controller.validation;

import com.att.tdp.popcorn_palace.controller.BookingController;
import com.att.tdp.popcorn_palace.dto.BookingDTO;
import com.att.tdp.popcorn_palace.dto.BookingResponseDTO;
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
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
public class BookingControllerValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingService bookingService;

    @Test
    public void bookTicket_NullShowtimeId_ReturnsBadRequest() throws Exception {
        BookingDTO bookingDTO = new BookingDTO();
        bookingDTO.setShowtimeId(null);
        bookingDTO.setSeatNumber(10);
        bookingDTO.setUserId(UUID.randomUUID());

        // Simulate service layer throwing IllegalArgumentException
        doThrow(new IllegalArgumentException("Showtime ID is required for a booking"))
            .when(bookingService).bookTicket(any(BookingDTO.class));

        mockMvc.perform(post("/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bookingDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void bookTicket_InvalidSeatNumber_ReturnsBadRequest() throws Exception {
        BookingDTO bookingDTO = new BookingDTO();
        bookingDTO.setShowtimeId(1L);
        bookingDTO.setSeatNumber(0);  // Invalid seat number
        bookingDTO.setUserId(UUID.randomUUID());

        // Simulate service layer throwing IllegalArgumentException
        doThrow(new IllegalArgumentException("Seat number must be greater than 0"))
            .when(bookingService).bookTicket(any(BookingDTO.class));

        mockMvc.perform(post("/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bookingDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void bookTicket_NegativeSeatNumber_ReturnsBadRequest() throws Exception {
        BookingDTO bookingDTO = new BookingDTO();
        bookingDTO.setShowtimeId(1L);
        bookingDTO.setSeatNumber(-5);  // Negative seat number
        bookingDTO.setUserId(UUID.randomUUID());

        // Simulate service layer throwing IllegalArgumentException
        doThrow(new IllegalArgumentException("Seat number must be greater than 0"))
            .when(bookingService).bookTicket(any(BookingDTO.class));

        mockMvc.perform(post("/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bookingDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void bookTicket_NullUserId_ReturnsBadRequest() throws Exception {
        BookingDTO bookingDTO = new BookingDTO();
        bookingDTO.setShowtimeId(1L);
        bookingDTO.setSeatNumber(10);
        bookingDTO.setUserId(null);  // Null user ID

        // Simulate service layer throwing IllegalArgumentException
        doThrow(new IllegalArgumentException("User ID is required for a booking"))
            .when(bookingService).bookTicket(any(BookingDTO.class));

        mockMvc.perform(post("/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bookingDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void bookTicket_ValidData_ReturnsOk() throws Exception {
        BookingDTO bookingDTO = new BookingDTO();
        bookingDTO.setShowtimeId(1L);
        bookingDTO.setSeatNumber(10);
        bookingDTO.setUserId(UUID.randomUUID());

        when(bookingService.bookTicket(any(BookingDTO.class)))
                .thenReturn(new BookingResponseDTO(UUID.randomUUID()));

        mockMvc.perform(post("/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bookingDTO)))
                .andExpect(status().isOk());
    }
}
