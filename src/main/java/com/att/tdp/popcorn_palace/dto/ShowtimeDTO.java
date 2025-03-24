package com.att.tdp.popcorn_palace.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.validation.constraints.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShowtimeDTO {
    private Long id;
    
    @Positive(message = "Price must be greater than 0")
    private Double price;
    
    @NotNull(message = "Movie ID is required")
    private Long movieId;
    
    @NotBlank(message = "Theater name is required")
    private String theater;
    
    @NotNull(message = "Start time is required")
    private LocalDateTime startTime;
    
    @NotNull(message = "End time is required")
    private LocalDateTime endTime;
}
