package com.att.tdp.popcorn_palace.exception;

// Seat Already Booked Exception
public class SeatAlreadyBookedException extends RuntimeException {
    public SeatAlreadyBookedException(String message) {
        super(message);
    }
}
