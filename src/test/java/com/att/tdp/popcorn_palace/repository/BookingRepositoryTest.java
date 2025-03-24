package com.att.tdp.popcorn_palace.repository;

import com.att.tdp.popcorn_palace.entity.Booking;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class BookingRepositoryTest {

    @Autowired
    private BookingRepository bookingRepository;

    @Test
    public void testSaveBooking() {
        // Create a new booking
        Booking booking = new Booking();
        booking.setShowtimeId(1L);
        booking.setSeatNumber(10);
        booking.setUserId(UUID.randomUUID());

        // Save the booking
        Booking savedBooking = bookingRepository.save(booking);

        // Verify the booking was saved
        assertNotNull(savedBooking.getBookingId());
        assertEquals(1L, savedBooking.getShowtimeId());
        assertEquals(10, savedBooking.getSeatNumber());
    }

    @Test
    public void testFindById() {
        // Create and save a booking
        Booking booking = new Booking();
        booking.setShowtimeId(1L);
        booking.setSeatNumber(11);
        booking.setUserId(UUID.randomUUID());
        
        Booking savedBooking = bookingRepository.save(booking);

        // Find booking by ID
        Optional<Booking> foundBooking = bookingRepository.findById(savedBooking.getBookingId());

        // Verify the booking was found
        assertTrue(foundBooking.isPresent());
        assertEquals(savedBooking.getBookingId(), foundBooking.get().getBookingId());
        assertEquals(11, foundBooking.get().getSeatNumber());
    }

    @Test
    public void testExistsByShowtimeIdAndSeatNumber_True() {
        // Create and save a booking
        Booking booking = new Booking();
        booking.setShowtimeId(2L);
        booking.setSeatNumber(20);
        booking.setUserId(UUID.randomUUID());
        bookingRepository.save(booking);

        // Check if booking exists
        boolean exists = bookingRepository.existsByShowtimeIdAndSeatNumber(2L, 20);

        // Verify booking exists
        assertTrue(exists);
    }

    @Test
    public void testExistsByShowtimeIdAndSeatNumber_False() {
        // Check if non-existent booking exists
        boolean exists = bookingRepository.existsByShowtimeIdAndSeatNumber(999L, 999);

        // Verify booking does not exist
        assertFalse(exists);
    }
}
