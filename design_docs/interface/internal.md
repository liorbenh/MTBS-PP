# Internal API 

## 1. Movie Interface

### Controller Layer (MovieController.java)
#### URL: /movies

@GetMapping("/all")
public ResponseEntity<List<MovieDTO>> getAllMovies();

@PostMapping
public ResponseEntity<MovieDTO> addMovie(@RequestBody MovieDTO movieDTO);

@PutMapping("/update/{title}")
public ResponseEntity<MovieDTO> updateMovie(@PathVariable String title, @RequestBody MovieDTO movieDTO);

@DeleteMapping("/{title}")
public ResponseEntity<Void> deleteMovie(@PathVariable String title);

### Service Layer (MovieService.java)

public List<MovieDTO> getAllMovies();

public MovieDTO getMovieByTitle(String title);

public MovieDTO addMovie(MovieDTO movieDto);

public MovieDTO updateMovie(String movieTitle, MovieDTO movieDto);

public void deleteMovie(String movieTitle);

// Helper methods for DTO to Entity conversion
private MovieDTO convertToDTO(Movie movie);

private Movie convertToEntity(MovieDTO movieDTO);

### Repository Layer (MovieRepository.java)

Optional<Movie> findByTitle(String title);

void deleteByTitle(String title);



## 2. Showtime Interface

### Controller Layer (ShowtimeController.java)
#### URL: /showtimes

// Get a specific showtime by ID
@GetMapping("/{id}")
public ResponseEntity<ShowtimeDTO> getShowtimeById(@PathVariable Long id);

// Get all showtimes
@GetMapping("/all")
public ResponseEntity<List<ShowtimeDTO>> getAllShowtimes();

// Add a new showtime
@PostMapping
public ResponseEntity<ShowtimeDTO> addShowtime(@RequestBody ShowtimeDTO showtimeDTO);

// Update an existing showtime
@PutMapping("/update/{id}")
public ResponseEntity<ShowtimeDTO> updateShowtime(@PathVariable Long id, @RequestBody ShowtimeDTO showtimeDTO);

// Delete a showtime by ID
@DeleteMapping("/{id}")
public ResponseEntity<Void> deleteShowtime(@PathVariable Long id);

### Service Layer (ShowtimeService.java)

public ShowtimeDTO getShowtimeById(Long id);

public List<ShowtimeDTO> getAllShowtimes();

public ShowtimeDTO addShowtime(ShowtimeDTO showtimeDto);
// Uses existing theaters - will throw exception if theater doesn't exist

public ShowtimeDTO updateShowtime(Long showtimeId, ShowtimeDTO showtimeDto);
// Uses existing theaters - will throw exception if theater doesn't exist

public void deleteShowtime(Long showtimeId);

public boolean isShowtimeOverlapping(String theater, LocalDateTime startTime, LocalDateTime endTime);

// Helper methods for DTO to Entity conversion
private ShowtimeDTO convertToDTO(Showtime showtime);

private Showtime convertToEntity(ShowtimeDTO showtimeDTO);

### Repository Layer (ShowtimeRepository.java)

public List<Showtime> findByTheaterAndTimeRange(String theater, LocalDateTime start, LocalDateTime end);



## 3. Booking Interface

### Controller Layer (BookingController.java)
#### URL: /bookings

@PostMapping
public ResponseEntity<BookingResponseDTO> bookTicket(@RequestBody BookingDTO bookingDTO);

### Service Layer (BookingService.java)
public BookingDTO bookTicket(BookingDTO bookingDto);

// Helper methods for DTO to Entity conversion
private BookingDTO convertToDTO(Booking booking);

private Booking convertToEntity(BookingDTO bookingDTO);

### Repository Layer (BookingRepository.java)

public boolean existsByShowtimeIdAndSeatNumber(Long showtimeId, int seatNumber)


## 4. Theater Interface

### Controller Layer (TheaterController.java)
#### URL: /theaters

public ResponseEntity<List<TheaterDTO>> getAllTheaters()

@GetMapping("/{id}")
public ResponseEntity<TheaterDTO> getTheaterById(@PathVariable Long id)

@PostMapping
public ResponseEntity<TheaterDTO> createTheater(@RequestBody TheaterDTO theaterDTO)


### Service Layer (TheaterService.java)

public TheaterDTO getTheaterById(Long id);

public List<TheaterDTO> getAllTheaters();

public TheaterDTO getTheaterByName(String theaterName);
// Returns existing theater or throws exception if not found

public TheaterDTO addTheater(TheaterDTO theaterDto);
// SERIALIZED ISOLATION LEVEL
// This action will create seats based on numberOfSeats attribute (default 100)
// Limited to 500 theaters maximum - will throw exception if limit exceeded

private void createSeatsForTheater(Long theaterId, Integer numberOfSeats);
// Creates the specified number of seats for a theater

// Helper methods for DTO to Entity conversion
private TheaterDTO convertToDTO(Theater theater);

private Theater convertToEntity(TheaterDTO theaterDTO);

### Repository Layer (TheaterRepository.java)

public Optional<Theater> findByName(String name);

## 5. ShowSeat Interface

### Service Layer (ShowSeatService.java)

public List<ShowSeatDTO> getSeatsByShowtimeId(Long showtimeId);

public boolean reserveSeat(Long showtimeId, Long seatId);

// Helper methods for DTO to Entity conversion
private ShowSeatDTO convertToDTO(ShowSeat showSeat);

private ShowSeat convertToEntity(ShowSeatDTO showSeatDTO);

### Repository Layer (ShowSeatRepository.java)

public Optional<ShowSeat> findByShowtimeIdAndSeatId(Long showtimeId, Long seatId);



## 6. Seat Interface

### Service Layer (SeatService.java)

public List<SeatDTO> getSeatsByTheater(Long theaterId);

public SeatDTO addSeat(SeatDTO seatDto)

// Helper methods for DTO to Entity conversion
private SeatDTO convertToDTO(Seat seat);

private Seat convertToEntity(SeatDTO seatDTO);

### Repository Layer (SeatRepository.java)

public List<Seat> findByTheaterId(Long theaterId);

