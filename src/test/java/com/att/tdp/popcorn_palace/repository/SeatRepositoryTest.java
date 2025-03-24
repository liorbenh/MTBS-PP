package com.att.tdp.popcorn_palace.repository;

import com.att.tdp.popcorn_palace.entity.Seat;
import com.att.tdp.popcorn_palace.entity.Theater;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class SeatRepositoryTest {

    @Autowired
    private SeatRepository seatRepository;
    
    @Autowired
    private TheaterRepository theaterRepository;
    
    private Long theaterId;

    @BeforeEach
    public void setUp() {
        // Create a theater for the seats
        Theater theater = new Theater();
        theater.setName("Seat Test Theater");
        Theater savedTheater = theaterRepository.save(theater);
        theaterId = savedTheater.getId();
    }

    @Test
    public void testSaveSeat() {
        // Create a new seat
        Seat seat = new Seat();
        seat.setTheaterId(theaterId);
        seat.setNumber(1);

        // Save the seat
        Seat savedSeat = seatRepository.save(seat);

        // Verify the seat was saved
        assertNotNull(savedSeat.getId());
        assertEquals(1, savedSeat.getNumber());
        assertEquals(theaterId, savedSeat.getTheaterId());
    }

    @Test
    public void testFindByTheaterId() {
        // Create and save several seats for the theater
        for (int i = 1; i <= 5; i++) {
            Seat seat = new Seat();
            seat.setTheaterId(theaterId);
            seat.setNumber(i);
            seatRepository.save(seat);
        }

        // Find seats by theater ID
        List<Seat> seats = seatRepository.findByTheaterId(theaterId);

        // Verify seats were found
        assertEquals(5, seats.size());
        assertTrue(seats.stream().allMatch(s -> s.getTheaterId().equals(theaterId)));
        
        // Verify seat numbers
        for (int i = 1; i <= 5; i++) {
            final int num = i;
            assertTrue(seats.stream().anyMatch(s -> s.getNumber() == num));
        }
    }

    @Test
    public void testFindByTheaterIdAndNumber() {
        // Create and save a seat
        Seat seat = new Seat();
        seat.setTheaterId(theaterId);
        seat.setNumber(10);
        seatRepository.save(seat);

        // Find seat by theater ID and number
        Optional<Seat> foundSeat = seatRepository.findByTheaterIdAndNumber(theaterId, 10);

        // Verify the seat was found
        assertTrue(foundSeat.isPresent());
        assertEquals(10, foundSeat.get().getNumber());
        assertEquals(theaterId, foundSeat.get().getTheaterId());
    }

    @Test
    public void testFindByTheaterIdAndNumber_NotFound() {
        // Try to find a non-existent seat
        Optional<Seat> notFoundSeat = seatRepository.findByTheaterIdAndNumber(theaterId, 999);

        // Verify no seat was found
        assertFalse(notFoundSeat.isPresent());
    }

    @Test
    public void testFindByNonExistentTheaterId() {
        // Find seats by a non-existent theater ID
        List<Seat> seats = seatRepository.findByTheaterId(999L);

        // Verify no seats were found
        assertTrue(seats.isEmpty());
    }
}
