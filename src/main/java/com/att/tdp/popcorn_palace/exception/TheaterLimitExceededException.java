package com.att.tdp.popcorn_palace.exception;

import java.util.List;

public class TheaterLimitExceededException extends RuntimeException {
    
    private final List<String> availableTheaters;
    
    public TheaterLimitExceededException(String message, List<String> availableTheaters) {
        super(message);
        this.availableTheaters = availableTheaters;
    }
    
    public List<String> getAvailableTheaters() {
        return availableTheaters;
    }
}
