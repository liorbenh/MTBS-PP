package com.att.tdp.popcorn_palace.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.validation.constraints.*;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingDTO {
    private UUID bookingId;
    
    @NotNull(message = "Showtime ID is required")
    private Long showtimeId;
    
    @Positive(message = "Seat number must be greater than 0")
    private Integer seatNumber;
    
    @NotNull(message = "User ID is required")
    private UUID userId;
}
