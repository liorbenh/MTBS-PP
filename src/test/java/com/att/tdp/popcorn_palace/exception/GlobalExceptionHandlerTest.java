package com.att.tdp.popcorn_palace.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler globalExceptionHandler;

    @BeforeEach
    public void setUp() {
        globalExceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    public void testHandleResourceNotFoundException() {
        // Arrange
        ResourceNotFoundException exception = new ResourceNotFoundException("Resource not found");
        
        // Act
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleResourceNotFoundException(exception);
        
        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Resource not found", response.getBody().getMessage());
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getBody().getStatus());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    public void testHandleResourceAlreadyExistsException() {
        // Arrange
        ResourceAlreadyExistsException exception = new ResourceAlreadyExistsException("Resource already exists");
        
        // Act
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleResourceAlreadyExistsException(exception);
        
        // Assert
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Resource already exists", response.getBody().getMessage());
        assertEquals(HttpStatus.CONFLICT.value(), response.getBody().getStatus());
    }

    @Test
    public void testHandleShowtimeOverlapException() {
        // Arrange
        ShowtimeOverlapException exception = new ShowtimeOverlapException("Showtime overlap detected");
        
        // Act
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleShowtimeOverlapException(exception);
        
        // Assert
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Showtime overlap detected", response.getBody().getMessage());
        assertEquals(HttpStatus.CONFLICT.value(), response.getBody().getStatus());
    }

    @Test
    public void testHandleSeatAlreadyBookedException() {
        // Arrange
        SeatAlreadyBookedException exception = new SeatAlreadyBookedException("Seat already booked");
        
        // Act
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleSeatAlreadyBookedException(exception);
        
        // Assert
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Seat already booked", response.getBody().getMessage());
        assertEquals(HttpStatus.CONFLICT.value(), response.getBody().getStatus());
    }

    @Test
    public void testHandleMethodArgumentNotValidException() {
        // Arrange
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        
        List<FieldError> fieldErrors = new ArrayList<>();
        fieldErrors.add(new FieldError("object", "field1", "Field1 error"));
        fieldErrors.add(new FieldError("object", "field2", "Field2 error"));
        
        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(new ArrayList<>(fieldErrors));
        
        // Act
        ResponseEntity<Map<String, String>> response = globalExceptionHandler.handleValidationExceptions(exception);
        
        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Field1 error", response.getBody().get("field1"));
        assertEquals("Field2 error", response.getBody().get("field2"));
    }

    @Test
    public void testHandleGenericException() {
        // Arrange
        Exception exception = new Exception("Unexpected error");
        
        // Act
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleGenericException(exception);
        
        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody().getMessage().contains("An unexpected error occurred"));
        assertTrue(response.getBody().getMessage().contains("Unexpected error"));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getBody().getStatus());
    }

    @Test
    public void testHandleIllegalArgumentException() {
        // Arrange
        IllegalArgumentException exception = new IllegalArgumentException("Invalid argument provided");
        
        // Act
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleIllegalArgumentException(exception);
        
        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid argument provided", response.getBody().getMessage());
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getBody().getStatus());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    public void testHandleNoHandlerFoundException() {
        // Arrange
        NoHandlerFoundException exception = new NoHandlerFoundException(
            "GET", "/invalid/path", null);
        
        // Act
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleNoHandlerFoundException(exception);
        
        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertTrue(response.getBody().getMessage().contains("/invalid/path"));
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getBody().getStatus());
    }

    @Test
    public void testHandleMethodNotSupportedException() {
        // Arrange
        HttpRequestMethodNotSupportedException exception = new HttpRequestMethodNotSupportedException(
            "POST", Arrays.asList("GET"));
        
        // Act
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleMethodNotSupportedException(exception);
        
        // Assert
        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatusCode());
        assertNotNull(response.getBody().getMessage());
        assertEquals(HttpStatus.METHOD_NOT_ALLOWED.value(), response.getBody().getStatus());
    }

    @Test
    public void testHandleTheaterLimitExceededException() {
        // Arrange
        List<String> availableTheaters = Arrays.asList("Theater 1", "Theater 2", "Theater 3");
        TheaterLimitExceededException exception = new TheaterLimitExceededException("Theater limit exceeded", availableTheaters);
        
        // Act
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleTheaterLimitExceededException(exception);
        
        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Theater limit exceeded", response.getBody().getMessage());
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getBody().getStatus());
        assertNotNull(response.getBody().getTimestamp());
    }
}
