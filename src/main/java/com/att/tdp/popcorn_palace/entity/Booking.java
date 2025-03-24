package com.att.tdp.popcorn_palace.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Entity
@Table(name = "bookings")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Booking {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID bookingId;
    
    @Column(name = "showtime_id", nullable = false)
    private Long showtimeId;
    
    @Column(name = "seat_number", nullable = false)
    private Integer seatNumber;
    
    @Column(name = "user_id", nullable = false)
    private UUID userId;
}
