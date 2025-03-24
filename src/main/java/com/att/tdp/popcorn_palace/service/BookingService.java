package com.att.tdp.popcorn_palace.service;

import com.att.tdp.popcorn_palace.dto.BookingDTO;
import com.att.tdp.popcorn_palace.dto.BookingResponseDTO;
import com.att.tdp.popcorn_palace.entity.Booking;
import com.att.tdp.popcorn_palace.entity.Seat;
import com.att.tdp.popcorn_palace.entity.Showtime;
import com.att.tdp.popcorn_palace.entity.Theater;
import com.att.tdp.popcorn_palace.exception.ResourceNotFoundException;
import com.att.tdp.popcorn_palace.exception.SeatAlreadyBookedException;
import com.att.tdp.popcorn_palace.repository.BookingRepository;
import com.att.tdp.popcorn_palace.repository.ShowtimeRepository;
import com.att.tdp.popcorn_palace.repository.TheaterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final ShowtimeRepository showtimeRepository;
    private final TheaterRepository theaterRepository;
    private final SeatService seatService;
    private final ShowSeatService showSeatService;

    @Autowired
    public BookingService(
            BookingRepository bookingRepository,
            ShowtimeRepository showtimeRepository,
            TheaterRepository theaterRepository,
            SeatService seatService,
            ShowSeatService showSeatService) {
        this.bookingRepository = bookingRepository;
        this.showtimeRepository = showtimeRepository;
        this.theaterRepository = theaterRepository;
        this.seatService = seatService;
        this.showSeatService = showSeatService;
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public BookingResponseDTO bookTicket(BookingDTO bookingDto) {
        validateBookingData(bookingDto);

        // Validate showtime exists
        Showtime showtime = showtimeRepository.findById(bookingDto.getShowtimeId())
                .orElseThrow(() -> new ResourceNotFoundException("Showtime not found with id: " + bookingDto.getShowtimeId()));

        // Get theater
        Theater theater = theaterRepository.findByName(showtime.getTheater())
                .orElseThrow(() -> new ResourceNotFoundException("Theater not found with name: " + showtime.getTheater()));

        // Check if seat number is valid for this theater
        if (bookingDto.getSeatNumber() <= 0 || bookingDto.getSeatNumber() > theater.getNumberOfSeats()) {
            throw new IllegalArgumentException("Invalid seat number. Must be between 1 and " + theater.getNumberOfSeats());
        }

        // Find seat by theater and seat number
        Seat seat = seatService.findByTheaterIdAndNumber(theater.getId(), bookingDto.getSeatNumber());

        // Check if seat is already booked
        if (bookingRepository.existsByShowtimeIdAndSeatNumber(bookingDto.getShowtimeId(), bookingDto.getSeatNumber())) {
            throw new SeatAlreadyBookedException("Seat " + bookingDto.getSeatNumber() + " is already booked for this showtime.");
        }

        // Reserve seat - according to data flow
        boolean reserved = showSeatService.reserveSeat(showtime.getId(), seat.getId());
        if (!reserved) {
            throw new SeatAlreadyBookedException("Seat " + bookingDto.getSeatNumber() + " is already booked for this showtime.");
        }

        // Create booking
        Booking booking = new Booking();
        booking.setShowtimeId(bookingDto.getShowtimeId());
        booking.setSeatNumber(bookingDto.getSeatNumber());
        booking.setUserId(bookingDto.getUserId());

        Booking savedBooking = bookingRepository.save(booking);
        
        return new BookingResponseDTO(savedBooking.getBookingId());
    }

    /**
     * Validates the booking data according to the data modeling requirements
     * @param bookingDTO the booking data to validate
     * @throws IllegalArgumentException if validation fails
     */
    private void validateBookingData(BookingDTO bookingDTO) {
        if (bookingDTO == null) {
            throw new IllegalArgumentException("Booking data cannot be null");
        }
        
        if (bookingDTO.getShowtimeId() == null) {
            throw new IllegalArgumentException("Showtime ID is required for a booking");
        }
        
        if (bookingDTO.getSeatNumber() == null || bookingDTO.getSeatNumber() <= 0) {
            throw new IllegalArgumentException("Seat number must be greater than 0");
        }
        
        if (bookingDTO.getUserId() == null) {
            throw new IllegalArgumentException("User ID is required for a booking");
        }
    }

    // Helper methods for DTO to Entity conversion
    private BookingDTO convertToDTO(Booking booking) {
        return new BookingDTO(
                booking.getBookingId(),
                booking.getShowtimeId(),
                booking.getSeatNumber(),
                booking.getUserId()
        );
    }

    private Booking convertToEntity(BookingDTO bookingDTO) {
        return new Booking(
                bookingDTO.getBookingId(),
                bookingDTO.getShowtimeId(),
                bookingDTO.getSeatNumber(),
                bookingDTO.getUserId()
        );
    }
}
