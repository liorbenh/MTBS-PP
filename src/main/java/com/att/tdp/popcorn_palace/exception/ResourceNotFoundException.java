package com.att.tdp.popcorn_palace.exception;

// Resource Not Found Exception
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
