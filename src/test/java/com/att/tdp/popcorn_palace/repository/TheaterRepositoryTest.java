package com.att.tdp.popcorn_palace.repository;

import com.att.tdp.popcorn_palace.entity.Theater;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class TheaterRepositoryTest {

    @Autowired
    private TheaterRepository theaterRepository;

    @Test
    public void testSaveTheater() {
        // Create a new theater
        Theater theater = new Theater();
        theater.setName("Cineplex");
        theater.setNumberOfSeats(150);

        // Save the theater
        Theater savedTheater = theaterRepository.save(theater);

        // Verify the theater was saved
        assertNotNull(savedTheater.getId());
        assertEquals("Cineplex", savedTheater.getName());
        assertEquals(150, savedTheater.getNumberOfSeats());
    }

    @Test
    public void testSaveTheaterWithDefaultSeats() {
        // Create a new theater without setting number of seats
        Theater theater = new Theater();
        theater.setName("DefaultSeatsTheater");

        // Save the theater
        Theater savedTheater = theaterRepository.save(theater);

        // Verify the theater was saved with default 100 seats
        assertNotNull(savedTheater.getId());
        assertEquals("DefaultSeatsTheater", savedTheater.getName());
        assertEquals(100, savedTheater.getNumberOfSeats());
    }

    @Test
    public void testFindByName() {
        // Create and save a theater
        Theater theater = new Theater();
        theater.setName("AMC");
        theater.setNumberOfSeats(120);
        theaterRepository.save(theater);

        // Find theater by name
        Optional<Theater> foundTheater = theaterRepository.findByName("AMC");

        // Verify the theater was found
        assertTrue(foundTheater.isPresent());
        assertEquals("AMC", foundTheater.get().getName());
        assertEquals(120, foundTheater.get().getNumberOfSeats());
    }

    @Test
    public void testFindByName_NotFound() {
        // Try to find a non-existent theater
        Optional<Theater> notFoundTheater = theaterRepository.findByName("Non-existent Theater");

        // Verify no theater was found
        assertFalse(notFoundTheater.isPresent());
    }

    @Test
    public void testFindAll() {
        // Create and save two theaters
        Theater theater1 = new Theater();
        theater1.setName("Theater 1");
        theater1.setNumberOfSeats(100);
        theaterRepository.save(theater1);

        Theater theater2 = new Theater();
        theater2.setName("Theater 2");
        theater2.setNumberOfSeats(150);
        theaterRepository.save(theater2);

        // Find all theaters
        List<Theater> theaters = theaterRepository.findAll();

        // Verify both theaters were found
        assertTrue(theaters.size() >= 2);
        assertTrue(theaters.stream().anyMatch(t -> t.getName().equals("Theater 1")));
        assertTrue(theaters.stream().anyMatch(t -> t.getName().equals("Theater 2")));
    }

    @Test
    public void testCount() {
        // Get initial count
        long initialCount = theaterRepository.count();
        
        // Add two theaters
        Theater theater1 = new Theater();
        theater1.setName("Count Theater 1");
        theaterRepository.save(theater1);

        Theater theater2 = new Theater();
        theater2.setName("Count Theater 2");
        theaterRepository.save(theater2);
        
        // Verify count increased by 2
        assertEquals(initialCount + 2, theaterRepository.count());
    }
}
