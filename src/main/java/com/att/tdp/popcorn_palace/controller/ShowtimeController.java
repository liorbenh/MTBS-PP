package com.att.tdp.popcorn_palace.controller;

import com.att.tdp.popcorn_palace.dto.ShowtimeDTO;
import com.att.tdp.popcorn_palace.service.ShowtimeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/showtimes")
public class ShowtimeController {

    private final ShowtimeService showtimeService;

    @Autowired
    public ShowtimeController(ShowtimeService showtimeService) {
        this.showtimeService = showtimeService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<ShowtimeDTO> getShowtimeById(@PathVariable Long id) {
        ShowtimeDTO showtime = showtimeService.getShowtimeById(id);
        return ResponseEntity.ok(showtime);
    }

    @GetMapping("/all")
    public ResponseEntity<List<ShowtimeDTO>> getAllShowtimes() {
        List<ShowtimeDTO> showtimes = showtimeService.getAllShowtimes();
        return ResponseEntity.ok(showtimes);
    }

    @PostMapping
    public ResponseEntity<ShowtimeDTO> addShowtime(@Valid @RequestBody ShowtimeDTO showtimeDTO) {
        ShowtimeDTO createdShowtime = showtimeService.addShowtime(showtimeDTO);
        return ResponseEntity.ok(createdShowtime);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<ShowtimeDTO> updateShowtime(@PathVariable Long id, @Valid @RequestBody ShowtimeDTO showtimeDTO) {
        ShowtimeDTO updatedShowtime = showtimeService.updateShowtime(id, showtimeDTO);
        return ResponseEntity.ok(updatedShowtime);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteShowtime(@PathVariable Long id) {
        showtimeService.deleteShowtime(id);
        return ResponseEntity.ok().build();
    }
}
