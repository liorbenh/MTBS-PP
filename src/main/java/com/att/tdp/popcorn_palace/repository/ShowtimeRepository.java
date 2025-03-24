package com.att.tdp.popcorn_palace.repository;

import com.att.tdp.popcorn_palace.entity.Showtime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ShowtimeRepository extends JpaRepository<Showtime, Long> {
    
    @Query("SELECT s FROM Showtime s WHERE s.theater = :theater AND " +
           "((s.startTime <= :endTime AND s.endTime >= :startTime) OR " +
           "(s.startTime >= :startTime AND s.startTime < :endTime) OR " +
           "(s.endTime > :startTime AND s.endTime <= :endTime))")
    List<Showtime> findByTheaterAndTimeRange(
            @Param("theater") String theater,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    List<Showtime> findByMovieId(Long movieId);
}
