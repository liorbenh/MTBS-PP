package com.att.tdp.popcorn_palace.repository;

import com.att.tdp.popcorn_palace.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {
    List<Seat> findByTheaterId(Long theaterId);
    
    @Query("SELECT s FROM Seat s WHERE s.theaterId = :theaterId AND s.number = :seatNumber")
    Optional<Seat> findByTheaterIdAndNumber(@Param("theaterId") Long theaterId, @Param("seatNumber") Integer seatNumber);
}
