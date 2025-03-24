package com.att.tdp.popcorn_palace.repository;

import com.att.tdp.popcorn_palace.entity.ShowSeat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class ShowSeatRepositoryTest {

    @Autowired
    private ShowSeatRepository showSeatRepository;

    @Test
    public void testSaveShowSeat() {
        // Create a new show seat
        ShowSeat showSeat = new ShowSeat();
        showSeat.setShowtimeId(1L);
        showSeat.setSeatId(1L);
        showSeat.setIsAvailable(true);

        // Save the show seat
        ShowSeat savedShowSeat = showSeatRepository.save(showSeat);

        // Verify the show seat was saved
        assertNotNull(savedShowSeat.getId());
        assertEquals(1L, savedShowSeat.getShowtimeId());
        assertEquals(1L, savedShowSeat.getSeatId());
        assertTrue(savedShowSeat.getIsAvailable());
    }

    @Test
    public void testFindByShowtimeId() {
        // Create and save several show seats for the same showtime
        for (int i = 1; i <= 5; i++) {
            ShowSeat showSeat = new ShowSeat();
            showSeat.setShowtimeId(2L);
            showSeat.setSeatId((long) i);
            showSeat.setIsAvailable(i % 2 == 0); // Even seats are available
            showSeatRepository.save(showSeat);
        }

        // Find show seats by showtime ID
        List<ShowSeat> showSeats = showSeatRepository.findByShowtimeId(2L);

        // Verify show seats were found
        assertEquals(5, showSeats.size());
        assertTrue(showSeats.stream().allMatch(s -> s.getShowtimeId() == 2L));
        
        // Verify availability pattern (even seats are available)
        for (ShowSeat showSeat : showSeats) {
            assertEquals(showSeat.getSeatId() % 2 == 0, showSeat.getIsAvailable());
        }
    }

    @Test
    public void testFindByShowtimeIdAndSeatId() {
        // Create and save a show seat
        ShowSeat showSeat = new ShowSeat();
        showSeat.setShowtimeId(3L);
        showSeat.setSeatId(10L);
        showSeat.setIsAvailable(true);
        showSeatRepository.save(showSeat);

        // Find show seat by showtime ID and seat ID
        Optional<ShowSeat> foundShowSeat = showSeatRepository.findByShowtimeIdAndSeatId(3L, 10L);

        // Verify the show seat was found
        assertTrue(foundShowSeat.isPresent());
        assertEquals(3L, foundShowSeat.get().getShowtimeId());
        assertEquals(10L, foundShowSeat.get().getSeatId());
        assertTrue(foundShowSeat.get().getIsAvailable());
    }

    @Test
    public void testFindByShowtimeIdAndSeatId_NotFound() {
        // Try to find a non-existent show seat
        Optional<ShowSeat> notFoundShowSeat = showSeatRepository.findByShowtimeIdAndSeatId(999L, 999L);

        // Verify no show seat was found
        assertFalse(notFoundShowSeat.isPresent());
    }

    @Test
    @Transactional
    public void testLockRow() {
        // Create and save a show seat with known identifiers
        ShowSeat showSeat = new ShowSeat();
        showSeat.setShowtimeId(10L);
        showSeat.setSeatId(20L);
        showSeat.setIsAvailable(true);
        ShowSeat savedShowSeat = showSeatRepository.saveAndFlush(showSeat);
        assertNotNull(savedShowSeat.getId());

        // Call lockRow to force a row lock
        // This native query does nothing except force locking, so we expect no changes.
        showSeatRepository.lockRow(10L, 20L);

        // Retrieve the same row and verify that its contents haven't changed
        Optional<ShowSeat> lockedShowSeat = showSeatRepository.findByShowtimeIdAndSeatId(10L, 20L);
        assertTrue(lockedShowSeat.isPresent());
        assertEquals(10L, lockedShowSeat.get().getShowtimeId());
        assertEquals(20L, lockedShowSeat.get().getSeatId());
        assertTrue(lockedShowSeat.get().getIsAvailable());
    }

    @Test
    public void testDeleteByShowtimeId() {
        // Create and save several show seats for the same showtime
        for (int i = 1; i <= 3; i++) {
            ShowSeat showSeat = new ShowSeat();
            showSeat.setShowtimeId(4L);
            showSeat.setSeatId((long) i);
            showSeat.setIsAvailable(true);
            showSeatRepository.save(showSeat);
        }

        // Verify show seats exist
        List<ShowSeat> showSeatsBeforeDelete = showSeatRepository.findByShowtimeId(4L);
        assertEquals(3, showSeatsBeforeDelete.size());

        // Delete show seats by showtime ID
        showSeatRepository.deleteByShowtimeId(4L);

        // Verify show seats were deleted
        List<ShowSeat> showSeatsAfterDelete = showSeatRepository.findByShowtimeId(4L);
        assertTrue(showSeatsAfterDelete.isEmpty());
    }
}
