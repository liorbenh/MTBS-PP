package com.att.tdp.popcorn_palace.entity;

import com.att.tdp.popcorn_palace.constants.TheaterConstants;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "theaters")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Theater {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String name;
    
    @Column(nullable = false)
    private Integer numberOfSeats = TheaterConstants.DEFAULT_SEATS;
    
    public Theater(Long id, String name) {
        this.id = id;
        this.name = name;
        this.numberOfSeats = TheaterConstants.DEFAULT_SEATS;
    }
}
