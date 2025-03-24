package com.att.tdp.popcorn_palace.exception;

// Resource Already Exists Exception
public class ResourceAlreadyExistsException extends RuntimeException {
    public ResourceAlreadyExistsException(String message) {
        super(message);
    }
}
