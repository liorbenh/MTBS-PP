package com.att.tdp.popcorn_palace.integration;

import com.att.tdp.popcorn_palace.dto.MovieDTO;
import com.att.tdp.popcorn_palace.dto.ShowtimeDTO;
import com.att.tdp.popcorn_palace.dto.TheaterDTO;
import com.att.tdp.popcorn_palace.exception.ResourceNotFoundException;
import com.att.tdp.popcorn_palace.exception.ShowtimeOverlapException;
import com.att.tdp.popcorn_palace.service.MovieService;
import com.att.tdp.popcorn_palace.service.ShowtimeService;
import com.att.tdp.popcorn_palace.service.TheaterService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class ShowtimeManagementIntegrationTest {

    @Autowired
    private MovieService movieService;

    @Autowired
    private ShowtimeService showtimeService;
    
    @Autowired
    private TheaterService theaterService;
    
    private TheaterDTO theater;
    private MovieDTO movie;

    @BeforeEach
    public void setUp() {
        // Create a theater first
        TheaterDTO theaterDTO = new TheaterDTO(null, "Integration Test Theater", 100);
        theater = theaterService.addTheater(theaterDTO);
        
        // Create a movie
        MovieDTO movieDTO = new MovieDTO(
                null,
                "Showtime Test Movie",
                "Sci-Fi",
                150,
                9.0,
                2023
        );
        movie = movieService.addMovie(movieDTO);
    }

    @Test
    public void testShowtimeCreationAndOverlapHandling() {
        // 1. Create a showtime
        LocalDateTime startTime = LocalDateTime.now().plusDays(1).withHour(18).withMinute(0);
        LocalDateTime endTime = startTime.plusHours(2).plusMinutes(30);
        
        ShowtimeDTO showtimeDTO = new ShowtimeDTO(
                null,
                15.00,
                movie.getId(),
                theater.getName(),
                startTime,
                endTime
        );
        
        ShowtimeDTO savedShowtime = showtimeService.addShowtime(showtimeDTO);
        assertNotNull(savedShowtime.getId());
        
        // 2. Try to create an overlapping showtime (same time, same theater)
        ShowtimeDTO overlappingShowtimeDTO = new ShowtimeDTO(
                null,
                15.00,
                movie.getId(),
                theater.getName(),
                startTime.plusMinutes(30),
                endTime.plusMinutes(30)
        );
        
        // Should throw an exception due to overlap
        assertThrows(ShowtimeOverlapException.class, () -> 
            showtimeService.addShowtime(overlappingShowtimeDTO)
        );
        
        // 3. Test with non-existent theater
        ShowtimeDTO nonExistentTheaterDTO = new ShowtimeDTO(
                null,
                15.00,
                movie.getId(),
                "Non-existent Theater",
                startTime,
                endTime
        );
        
        // Should throw an exception due to theater not found
        assertThrows(RuntimeException.class, () -> 
            showtimeService.addShowtime(nonExistentTheaterDTO)
        );
        
        // 4. Create a non-overlapping showtime (different time)
        ShowtimeDTO nonOverlappingShowtimeDTO = new ShowtimeDTO(
                null,
                15.00,
                movie.getId(),
                theater.getName(),
                endTime.plusMinutes(30),
                endTime.plusHours(2).plusMinutes(30)
        );
        
        ShowtimeDTO savedNonOverlappingShowtime = showtimeService.addShowtime(nonOverlappingShowtimeDTO);
        assertNotNull(savedNonOverlappingShowtime.getId());
        
        // 5. Create a second theater and showtime
        TheaterDTO secondTheaterDTO = new TheaterDTO(null, "Second Theater", 150);
        TheaterDTO secondTheater = theaterService.addTheater(secondTheaterDTO);
        
        ShowtimeDTO differentTheaterShowtimeDTO = new ShowtimeDTO(
                null,
                15.00,
                movie.getId(),
                secondTheater.getName(),
                startTime,
                endTime
        );
        
        ShowtimeDTO savedDifferentTheaterShowtime = showtimeService.addShowtime(differentTheaterShowtimeDTO);
        assertNotNull(savedDifferentTheaterShowtime.getId());
        
        // 6. Update showtime to have overlap - should fail
        ShowtimeDTO updateToOverlapDTO = new ShowtimeDTO(
                savedNonOverlappingShowtime.getId(),
                15.00,
                movie.getId(),
                theater.getName(),
                startTime,
                endTime
        );
        
        assertThrows(ShowtimeOverlapException.class, () -> 
            showtimeService.updateShowtime(savedNonOverlappingShowtime.getId(), updateToOverlapDTO)
        );
        
        // 7. Update showtime without overlap - should succeed
        ShowtimeDTO updateWithoutOverlapDTO = new ShowtimeDTO(
                savedNonOverlappingShowtime.getId(),
                20.00,  // Price change
                movie.getId(),
                theater.getName(),
                endTime.plusMinutes(30),
                endTime.plusHours(2).plusMinutes(30)
        );
        
        ShowtimeDTO updatedShowtime = showtimeService.updateShowtime(
                savedNonOverlappingShowtime.getId(), 
                updateWithoutOverlapDTO
        );
        
        assertEquals(20.00, updatedShowtime.getPrice());
    }
}
