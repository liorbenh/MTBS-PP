package com.att.tdp.popcorn_palace.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.validation.constraints.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovieDTO {
    private Long id;
    
    @NotBlank(message = "Title is required")
    private String title;
    
    @NotBlank(message = "Genre is required")
    private String genre;
    
    @Positive(message = "Duration must be greater than 0")
    private Integer duration;
    
    @DecimalMin(value = "0.0", message = "Rating must be at least 0.0")
    @DecimalMax(value = "10.0", message = "Rating must be at most 10.0")
    private Double rating;
    
    @Min(value = 1880, message = "Release year must be after 1880")
    private Integer releaseYear;
}
