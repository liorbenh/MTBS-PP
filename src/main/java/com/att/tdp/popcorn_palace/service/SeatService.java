package com.att.tdp.popcorn_palace.service;

import com.att.tdp.popcorn_palace.dto.SeatDTO;
import com.att.tdp.popcorn_palace.entity.Seat;
import com.att.tdp.popcorn_palace.repository.SeatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SeatService {

    private final SeatRepository seatRepository;

    @Autowired
    public SeatService(SeatRepository seatRepository) {
        this.seatRepository = seatRepository;
    }

    public List<SeatDTO> getSeatsByTheater(Long theaterId) {
        return seatRepository.findByTheaterId(theaterId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public SeatDTO addSeat(SeatDTO seatDTO) {
        validateSeatData(seatDTO);
        Seat seat = convertToEntity(seatDTO);
        Seat savedSeat = seatRepository.save(seat);
        return convertToDTO(savedSeat);
    }

    public Seat findByTheaterIdAndNumber(Long theaterId, Integer seatNumber) {
        return seatRepository.findByTheaterIdAndNumber(theaterId, seatNumber)
                .orElseThrow(() -> new RuntimeException("Seat not found with theater id: " + theaterId + " and number: " + seatNumber));
    }

    // Helper methods for DTO to Entity conversion
    private SeatDTO convertToDTO(Seat seat) {
        return new SeatDTO(
                seat.getId(),
                seat.getTheaterId(),
                seat.getNumber()
        );
    }

    private Seat convertToEntity(SeatDTO seatDTO) {
        return new Seat(
                seatDTO.getId(),
                seatDTO.getTheaterId(),
                seatDTO.getNumber()
        );
    }

    /**
     * Validates the seat data according to the data modeling requirements
     * @param seatDTO the seat data to validate
     * @throws IllegalArgumentException if validation fails
     */
    private void validateSeatData(SeatDTO seatDTO) {
        if (seatDTO == null) {
            throw new IllegalArgumentException("Seat data cannot be null");
        }
        
        if (seatDTO.getTheaterId() == null) {
            throw new IllegalArgumentException("Theater ID is required for a seat");
        }
        
        if (seatDTO.getNumber() == null || seatDTO.getNumber() <= 0) {
            throw new IllegalArgumentException("Seat number must be greater than 0");
        }
    }
}
