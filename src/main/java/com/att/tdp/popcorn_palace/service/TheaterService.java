package com.att.tdp.popcorn_palace.service;

import com.att.tdp.popcorn_palace.constants.TheaterConstants;
import com.att.tdp.popcorn_palace.dto.TheaterDTO;
import com.att.tdp.popcorn_palace.dto.SeatDTO;
import com.att.tdp.popcorn_palace.entity.Theater;
import com.att.tdp.popcorn_palace.exception.ResourceAlreadyExistsException;
import com.att.tdp.popcorn_palace.exception.ResourceNotFoundException;
import com.att.tdp.popcorn_palace.exception.TheaterLimitExceededException;
import com.att.tdp.popcorn_palace.repository.TheaterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class TheaterService {

    private final TheaterRepository theaterRepository;
    private final SeatService seatService;

    @Autowired
    public TheaterService(TheaterRepository theaterRepository, SeatService seatService) {
        this.theaterRepository = theaterRepository;
        this.seatService = seatService;
    }

    public TheaterDTO getTheaterById(Long id) {
        Theater theater = theaterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Theater not found with id: " + id));
        return convertToDTO(theater);
    }

    public List<TheaterDTO> getAllTheaters() {
        return theaterRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public TheaterDTO addTheater(TheaterDTO theaterDto) {
        validateTheaterData(theaterDto);

        // Check if we've reached the maximum number of theaters
        if (theaterRepository.count() >= TheaterConstants.MAX_THEATERS) {
            List<String> theaterNames = theaterRepository.findAll().stream()
                    .map(Theater::getName)
                    .collect(Collectors.toList());
            throw new TheaterLimitExceededException("Maximum number of theaters reached. Available theaters: " + theaterNames, theaterNames);
        }

        // Check if theater with this name already exists
        Optional<Theater> existingTheater = theaterRepository.findByName(theaterDto.getName());
        if (existingTheater.isPresent()) {
            throw new ResourceAlreadyExistsException("Theater with name '" + theaterDto.getName() + "' already exists.");
        }

        // Create and save the theater
        Theater theater = convertToEntity(theaterDto);
                Theater savedTheater = theaterRepository.save(theater);

        // Data flow - Create seats for the theater
        createSeatsForTheater(savedTheater.getId(), savedTheater.getNumberOfSeats());

        return convertToDTO(savedTheater);
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public TheaterDTO getTheaterByName(String theaterName) {
        Theater theater = theaterRepository.findByName(theaterName)
                .orElseThrow(() -> new ResourceNotFoundException("Theater not found with name: " + theaterName));
        return convertToDTO(theater);
    }

    private void createSeatsForTheater(Long theaterId, Integer numberOfSeats) {
        IntStream.rangeClosed(1, numberOfSeats)
                .forEach(seatNumber -> {
                    SeatDTO seatDTO = new SeatDTO(null, theaterId, seatNumber);
                    seatService.addSeat(seatDTO);
                });
    }

    // Helper methods for DTO to Entity conversion
    private TheaterDTO convertToDTO(Theater theater) {
        return new TheaterDTO(
                theater.getId(),
                theater.getName(),
                theater.getNumberOfSeats()
        );
    }

    private Theater convertToEntity(TheaterDTO theaterDTO) {
        Theater theater = new Theater();
        theater.setId(theaterDTO.getId());
        theater.setName(theaterDTO.getName());
        if (theaterDTO.getNumberOfSeats() != null) {
            theater.setNumberOfSeats(theaterDTO.getNumberOfSeats());
        }
        return theater;
    }

    /**
     * Validates the theater data according to the data modeling requirements
     * @param theaterDTO the theater data to validate
     * @throws IllegalArgumentException if validation fails
     */
    private void validateTheaterData(TheaterDTO theaterDTO) {
        if (theaterDTO == null) {
            throw new IllegalArgumentException("Theater data cannot be null");
        }
        
        if (theaterDTO.getName() == null || theaterDTO.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Theater name cannot be empty");
        }
        
        // If number of seats is provided, validate it
        if (theaterDTO.getNumberOfSeats() != null && theaterDTO.getNumberOfSeats() <= 0) {
            throw new IllegalArgumentException("Number of seats must be greater than 0");
        }
    }
}
