package com.att.tdp.popcorn_palace.service;

import com.att.tdp.popcorn_palace.dto.SeatDTO;
import com.att.tdp.popcorn_palace.dto.ShowtimeDTO;
import com.att.tdp.popcorn_palace.dto.TheaterDTO;
import com.att.tdp.popcorn_palace.entity.Showtime;
import com.att.tdp.popcorn_palace.exception.ResourceNotFoundException;
import com.att.tdp.popcorn_palace.exception.ShowtimeOverlapException;
import com.att.tdp.popcorn_palace.repository.BookingRepository;
import com.att.tdp.popcorn_palace.repository.MovieRepository;
import com.att.tdp.popcorn_palace.repository.ShowtimeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ShowtimeService {

    private final ShowtimeRepository showtimeRepository;
    private final MovieRepository movieRepository;
    private final BookingRepository bookingRepository;
    private final TheaterService theaterService;
    private final SeatService seatService;
    private final ShowSeatService showSeatService;

    @Autowired
    public ShowtimeService(
            ShowtimeRepository showtimeRepository,
            MovieRepository movieRepository,
            BookingRepository bookingRepository,
            TheaterService theaterService,
            SeatService seatService,
            ShowSeatService showSeatService) {
        this.showtimeRepository = showtimeRepository;
        this.movieRepository = movieRepository;
        this.bookingRepository = bookingRepository;
        this.theaterService = theaterService;
        this.seatService = seatService;
        this.showSeatService = showSeatService;
    }

    public ShowtimeDTO getShowtimeById(Long id) {
        Showtime showtime = showtimeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Showtime not found with id: " + id));
        return convertToDTO(showtime);
    }

    public List<ShowtimeDTO> getAllShowtimes() {
        List<Showtime> showtimes = showtimeRepository.findAll();
        return showtimes.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ShowtimeDTO addShowtime(ShowtimeDTO showtimeDto) {
        validateShowtimeData(showtimeDto);
        // Additional validations
        if (showtimeDto.getEndTime().isBefore(showtimeDto.getStartTime())) {
            throw new IllegalArgumentException("End time must be after start time");
        }
        
        // Validate movie exists
        if (!movieRepository.existsById(showtimeDto.getMovieId())) {
            throw new ResourceNotFoundException("Movie with id " + showtimeDto.getMovieId() + " does not exist.");
        }

        // Check for overlapping showtimes
        if (isShowtimeOverlapping(showtimeDto.getTheater(), showtimeDto.getStartTime(), showtimeDto.getEndTime())) {
            throw new ShowtimeOverlapException("There is already a showtime scheduled at this theater during the specified time.");
        }

        // Get theater by name
        TheaterDTO theaterDTO = theaterService.getTheaterByName(showtimeDto.getTheater());

        // Create and save the showtime
        Showtime showtime = convertToEntity(showtimeDto);
        Showtime savedShowtime = showtimeRepository.save(showtime);

        // Get all seats for the theater and collect their IDs directly
        List<Long> seatIds = seatService.getSeatsByTheater(theaterDTO.getId()).stream()
                .map(SeatDTO::getId)
                .collect(Collectors.toList());

        // Create show seats for this showtime
        showSeatService.createShowSeats(savedShowtime.getId(), seatIds);

        return convertToDTO(savedShowtime);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ShowtimeDTO updateShowtime(Long showtimeId, ShowtimeDTO showtimeDto) {
        validateShowtimeData(showtimeDto);

        
        // Validate showtime exists
        Showtime showtime = showtimeRepository.findById(showtimeId)
                .orElseThrow(() -> new ResourceNotFoundException("Showtime not found with id: " + showtimeId));

        // Validate movie exists
        if (!movieRepository.existsById(showtimeDto.getMovieId())) {
            throw new ResourceNotFoundException("Movie with id " + showtimeDto.getMovieId() + " does not exist.");
        }

        // If theater is changing, get theater by name
        if (!showtime.getTheater().equals(showtimeDto.getTheater())) {
            theaterService.getTheaterByName(showtimeDto.getTheater());
        }

        // Check for overlapping showtimes (excluding this showtime)
        if (isShowtimeOverlappingExcludingCurrent(showtimeId, showtimeDto.getTheater(), 
                showtimeDto.getStartTime(), showtimeDto.getEndTime())) {
            throw new ShowtimeOverlapException("There is already another showtime scheduled at this theater during the specified time.");
        }

        // Update showtime properties
        showtime.setMovieId(showtimeDto.getMovieId());
        showtime.setTheater(showtimeDto.getTheater());
        showtime.setStartTime(showtimeDto.getStartTime());
        showtime.setEndTime(showtimeDto.getEndTime());
        showtime.setPrice(showtimeDto.getPrice());

        Showtime updatedShowtime = showtimeRepository.save(showtime);
        return convertToDTO(updatedShowtime);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void deleteShowtime(Long showtimeId) {
        if (!showtimeRepository.existsById(showtimeId)) {
            throw new ResourceNotFoundException("Showtime not found with id: " + showtimeId);
        }
        
        // According to data flow, we need to delete related bookings first
        bookingRepository.deleteByShowtimeId(showtimeId);
        
        // Delete all associated show seats
        showSeatService.deleteShowSeatsByShowtime(showtimeId);
        
        // Delete the showtime
        showtimeRepository.deleteById(showtimeId);
    }

    public boolean isShowtimeOverlapping(String theater, LocalDateTime startTime, LocalDateTime endTime) {
        List<Showtime> overlappingShowtimes = showtimeRepository.findByTheaterAndTimeRange(theater, startTime, endTime);
        return !overlappingShowtimes.isEmpty();
    }

    private boolean isShowtimeOverlappingExcludingCurrent(Long showtimeId, String theater, 
                                                      LocalDateTime startTime, LocalDateTime endTime) {
        List<Showtime> overlappingShowtimes = showtimeRepository.findByTheaterAndTimeRange(theater, startTime, endTime);
        return overlappingShowtimes.stream()
                .anyMatch(showtime -> !showtime.getId().equals(showtimeId));
    }

    // Helper methods for DTO to Entity conversion
    private ShowtimeDTO convertToDTO(Showtime showtime) {
        return new ShowtimeDTO(
                showtime.getId(),
                showtime.getPrice(),
                showtime.getMovieId(),
                showtime.getTheater(),
                showtime.getStartTime(),
                showtime.getEndTime()
        );
    }

    private Showtime convertToEntity(ShowtimeDTO showtimeDTO) {
        return new Showtime(
                showtimeDTO.getId(),
                showtimeDTO.getPrice(),
                showtimeDTO.getMovieId(),
                showtimeDTO.getTheater(),
                showtimeDTO.getStartTime(),
                showtimeDTO.getEndTime()
        );
    }

    /**
     * Validates the showtime data according to the data modeling requirements
     * @param showtimeDTO the showtime data to validate
     * @throws IllegalArgumentException if validation fails
     */
    private void validateShowtimeData(ShowtimeDTO showtimeDTO) {
        if (showtimeDTO == null) {
            throw new IllegalArgumentException("Showtime data cannot be null");
        }
        
        if (showtimeDTO.getMovieId() == null) {
            throw new IllegalArgumentException("Movie ID is required for a showtime");
        }
        
        if (showtimeDTO.getPrice() == null || showtimeDTO.getPrice() <= 0.0) {
            throw new IllegalArgumentException("Showtime price must be greater than 0.0");
        }
        
        if (showtimeDTO.getTheater() == null || showtimeDTO.getTheater().trim().isEmpty()) {
            throw new IllegalArgumentException("Theater name is required for a showtime");
        }
        
        if (showtimeDTO.getStartTime() == null) {
            throw new IllegalArgumentException("Start time is required for a showtime");
        }
        
        if (showtimeDTO.getEndTime() == null) {
            throw new IllegalArgumentException("End time is required for a showtime");
        }
        
        if (showtimeDTO.getStartTime().isAfter(showtimeDTO.getEndTime()) || 
            showtimeDTO.getStartTime().equals(showtimeDTO.getEndTime())) {
            throw new IllegalArgumentException("End time must be after start time");
        }
    }
}
