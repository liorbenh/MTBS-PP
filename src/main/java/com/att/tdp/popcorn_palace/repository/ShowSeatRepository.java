package com.att.tdp.popcorn_palace.repository;

import com.att.tdp.popcorn_palace.entity.ShowSeat;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface ShowSeatRepository extends JpaRepository<ShowSeat, Long> {
    Optional<ShowSeat> findByShowtimeIdAndSeatId(Long showtimeId, Long seatId);
    List<ShowSeat> findByShowtimeId(Long showtimeId);
    void deleteByShowtimeId(Long showtimeId);
    
    @Modifying
    @Query(value = "UPDATE show_seats SET id = id WHERE showtime_id = :showtimeId AND seat_id = :seatId", nativeQuery = true)
    void lockRow(@Param("showtimeId") Long showtimeId, @Param("seatId") Long seatId);
}
