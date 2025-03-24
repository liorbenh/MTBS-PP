package com.att.tdp.popcorn_palace.repository;

import com.att.tdp.popcorn_palace.entity.Showtime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class ShowtimeRepositoryTest {

    @Autowired
    private ShowtimeRepository showtimeRepository;

    @Test
    public void testSaveShowtime() {
        // Create a new showtime
        Showtime showtime = new Showtime();
        showtime.setMovieId(1L);
        showtime.setTheater("Test Theater");
        showtime.setPrice(12.5);
        showtime.setStartTime(LocalDateTime.now().plusHours(1));
        showtime.setEndTime(LocalDateTime.now().plusHours(3));

        // Save the showtime
        Showtime savedShowtime = showtimeRepository.save(showtime);

        // Verify the showtime was saved
        assertNotNull(savedShowtime.getId());
        assertEquals("Test Theater", savedShowtime.getTheater());
    }

    @Test
    public void testFindByTheaterAndTimeRange_Overlapping() {
        // Create a showtime
        Showtime showtime = new Showtime();
        showtime.setMovieId(1L);
        showtime.setTheater("Overlapping Theater");
        showtime.setPrice(12.5);
        LocalDateTime baseTime = LocalDateTime.now().plusDays(1).withHour(18).withMinute(0);
        showtime.setStartTime(baseTime);
        showtime.setEndTime(baseTime.plusHours(2));
        showtimeRepository.save(showtime);

        // Test fully overlapping time range
        List<Showtime> overlapping1 = showtimeRepository.findByTheaterAndTimeRange(
                "Overlapping Theater", 
                baseTime.minusMinutes(30), 
                baseTime.plusHours(2).plusMinutes(30));
        
        // Test partially overlapping time range (starts before, ends during)
        List<Showtime> overlapping2 = showtimeRepository.findByTheaterAndTimeRange(
                "Overlapping Theater", 
                baseTime.minusHours(1), 
                baseTime.plusHours(1));
        
        // Test partially overlapping time range (starts during, ends after)
        List<Showtime> overlapping3 = showtimeRepository.findByTheaterAndTimeRange(
                "Overlapping Theater", 
                baseTime.plusHours(1), 
                baseTime.plusHours(3));
        
        // Test exactly matching time range
        List<Showtime> overlapping4 = showtimeRepository.findByTheaterAndTimeRange(
                "Overlapping Theater", 
                baseTime, 
                baseTime.plusHours(2));

        // Verify all queries found the overlapping showtime
        assertEquals(1, overlapping1.size());
        assertEquals(1, overlapping2.size());
        assertEquals(1, overlapping3.size());
        assertEquals(1, overlapping4.size());
    }

    @Test
    public void testFindByTheaterAndTimeRange_NonOverlapping() {
        // Create a showtime
        Showtime showtime = new Showtime();
        showtime.setMovieId(1L);
        showtime.setTheater("Non-overlapping Theater");
        showtime.setPrice(12.5);
        LocalDateTime baseTime = LocalDateTime.now().plusDays(1).withHour(18).withMinute(0);
        showtime.setStartTime(baseTime);
        showtime.setEndTime(baseTime.plusHours(2));
        showtimeRepository.save(showtime);

        // Test time range before the showtime
        List<Showtime> nonOverlapping1 = showtimeRepository.findByTheaterAndTimeRange(
                "Non-overlapping Theater", 
                baseTime.minusHours(3), 
                baseTime.minusHours(1));
        
        // Test time range after the showtime
        List<Showtime> nonOverlapping2 = showtimeRepository.findByTheaterAndTimeRange(
                "Non-overlapping Theater", 
                baseTime.plusHours(3), 
                baseTime.plusHours(5));
        
        // Test different theater
        List<Showtime> nonOverlapping3 = showtimeRepository.findByTheaterAndTimeRange(
                "Different Theater", 
                baseTime, 
                baseTime.plusHours(2));

        // Verify no showtimes were found
        assertEquals(0, nonOverlapping1.size());
        assertEquals(0, nonOverlapping2.size());
        assertEquals(0, nonOverlapping3.size());
    }
}
