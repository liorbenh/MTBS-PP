package com.att.tdp.popcorn_palace.dto;

import com.att.tdp.popcorn_palace.constants.TheaterConstants;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TheaterDTO {
    private Long id;
    
    @NotBlank(message = "Theater name is required")
    private String name;
    
    @Positive(message = "Number of seats must be greater than 0")
    private Integer numberOfSeats = TheaterConstants.DEFAULT_SEATS;
    
    public TheaterDTO(Long id, String name) {
        this.id = id;
        this.name = name;
        this.numberOfSeats = TheaterConstants.DEFAULT_SEATS;
    }
}
