package com.att.tdp.popcorn_palace.controller;

import com.att.tdp.popcorn_palace.dto.TheaterDTO;
import com.att.tdp.popcorn_palace.service.TheaterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/theaters")
public class TheaterController {

    private final TheaterService theaterService;

    @Autowired
    public TheaterController(TheaterService theaterService) {
        this.theaterService = theaterService;
    }

    @GetMapping("/all")
    public ResponseEntity<List<TheaterDTO>> getAllTheaters() {
        return ResponseEntity.ok(theaterService.getAllTheaters());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TheaterDTO> getTheaterById(@PathVariable Long id) {
        return ResponseEntity.ok(theaterService.getTheaterById(id));
    }

    @PostMapping
    public ResponseEntity<TheaterDTO> createTheater(@Valid @RequestBody TheaterDTO theaterDTO) {
        TheaterDTO createdTheater = theaterService.addTheater(theaterDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTheater);
    }
}
