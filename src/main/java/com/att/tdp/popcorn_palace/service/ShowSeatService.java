package com.att.tdp.popcorn_palace.service;

import com.att.tdp.popcorn_palace.dto.ShowSeatDTO;
import com.att.tdp.popcorn_palace.entity.ShowSeat;
import com.att.tdp.popcorn_palace.repository.ShowSeatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ShowSeatService {

    private final ShowSeatRepository showSeatRepository;

    @Autowired
    public ShowSeatService(ShowSeatRepository showSeatRepository) {
        this.showSeatRepository = showSeatRepository;
    }

    public void createShowSeats(Long showtimeId, List<Long> seatIds) {
        for (Long seatId : seatIds) {
            ShowSeat showSeat = new ShowSeat();
            showSeat.setShowtimeId(showtimeId);
            showSeat.setSeatId(seatId);
            showSeat.setIsAvailable(true);
            showSeatRepository.save(showSeat);
        }
    }

    @Transactional
    public void deleteShowSeatsByShowtime(Long showtimeId) {
        showSeatRepository.deleteByShowtimeId(showtimeId);
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public List<ShowSeatDTO> getSeatsByShowtimeId(Long showtimeId) {
        return showSeatRepository.findByShowtimeId(showtimeId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Retryable(
        maxAttempts = 3,
        backoff = @Backoff(delay = 100, multiplier = 2.5, maxDelay = 1000, random = true),
        include = {Exception.class}
    )
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public boolean reserveSeat(Long showtimeId, Long seatId) {
        showSeatRepository.lockRow(showtimeId, seatId);
        ShowSeat showSeat = showSeatRepository.findByShowtimeIdAndSeatId(showtimeId, seatId)
                .orElseThrow(() -> new RuntimeException("Show seat not found for showtime: " + showtimeId + " and seat: " + seatId));
        
        if (!showSeat.getIsAvailable()) {
            return false;
        }
        
        showSeat.setIsAvailable(false);
        showSeatRepository.save(showSeat);
        return true;
    }
    

    // Helper methods for DTO to Entity conversion
    private ShowSeatDTO convertToDTO(ShowSeat showSeat) {
        return new ShowSeatDTO(
                showSeat.getId(),
                showSeat.getShowtimeId(),
                showSeat.getSeatId(),
                showSeat.getIsAvailable()
        );
    }

    private ShowSeat convertToEntity(ShowSeatDTO showSeatDTO) {
        return new ShowSeat(
                showSeatDTO.getId(),
                showSeatDTO.getShowtimeId(),
                showSeatDTO.getSeatId(),
                showSeatDTO.getIsAvailable()
        );
    }
}
