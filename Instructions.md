# Popcorn Palace Movie Ticket Booking System

## Overview
Popcorn Palace is a backend service for managing movie showtime bookings. It allows users to view movies, showtimes, and make bookings through a set of RESTful APIs.

[System Design Documentation](./design_docs/main.md)

## Prerequisites
- Java 21 (tested with JDK 21.0.6)
- Maven 3.8+
- PostgreSQL 16+ (locally installed or Docker)

## Getting Started

### 1. Setting Up the Project

1. **Clone the repository**
   ```
   git clone https://github.com/yourusername/popcorn-palace.git
   cd popcorn-palace
   ```

2. **Database Setup**

   **Option 1: Using locally installed PostgreSQL**
   
   If you're using a locally installed PostgreSQL server:

   a. Connect to PostgreSQL as superuser:
   ```
   psql -U postgres -d postgres
   ```
   
   b. Create user and database:
   ```sql
   CREATE USER "popcorn-palace" WITH PASSWORD 'popcorn-palace';
   CREATE DATABASE "popcorn-palace";
   GRANT ALL PRIVILEGES ON DATABASE "popcorn-palace" TO "popcorn-palace";
   ```
   
   c. Connect to the newly created database and set permissions:
   ```sql
   \c popcorn-palace
   
   -- Grant schema permissions
   GRANT USAGE, CREATE ON SCHEMA public TO "popcorn-palace";
   
   -- Grant table permissions (including future tables)
   GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO "popcorn-palace";
   GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO "popcorn-palace";
   
   -- Make sure future tables will be accessible too
   ALTER DEFAULT PRIVILEGES IN SCHEMA public
   GRANT ALL PRIVILEGES ON TABLES TO "popcorn-palace";
   
   ALTER DEFAULT PRIVILEGES IN SCHEMA public
   GRANT ALL PRIVILEGES ON SEQUENCES TO "popcorn-palace";
   ```
   
   d. Update authentication configuration:
   
   Edit the pg_hba.conf file (typically in /etc/postgresql/16/main/):
   ```
   sudo nano /etc/postgresql/16/main/pg_hba.conf
   ```
   
   Find the line:
   ```
   local   all             all                                     peer
   ```
   
   Change it to:
   ```
   local   all             all                                     md5
   ```
   
   e. Restart PostgreSQL to apply changes:
   ```
   sudo systemctl restart postgresql
   ```

   **Option 2: Using Docker**

   If you prefer to use Docker, ensure that the `compose.yml` file is present in the root directory of the project. Then, run the following command:
   ```
   docker-compose -f compose.yml up -d
   ```
   This will start a PostgreSQL instance on port 5432 with the following credentials:
   - Database: popcorn-palace
   - Username: popcorn-palace
   - Password: popcorn-palace

### 2. Building the Project

The project uses Maven for build management:

1. **Compile and install the application**
   ```
   ./mvnw clean install
   ```

   Additional build commands:
   ```
   ./mvnw compile    # Compile only
   ./mvnw package    # Package into a JAR file (in 'target' directory)
   ```

### 3. Running the Application

1. **Start the application**
   ```
   ./mvnw spring-boot:run
   ```
   The application will be available at http://localhost:8080

### 4. Running Tests

The project includes comprehensive tests for all components:

1. **Run all tests**
   ```
   ./mvnw test
   ```

2. **Run specific test classes**
   ```
   ./mvnw test -Dtest=TheaterControllerTest
   ```

3. **Run integration tests only**
   ```
   ./mvnw test -Dtest=*IntegrationTest
   ```

The testing suite includes:
- Unit tests for all service, repository, and controller layers
- Validation tests for input validation
- Integration tests for end-to-end workflows
- Concurrency tests for booking operations

## API Endpoints

### Movies API
- `GET /movies/all` - Get all movies
- `POST /movies` - Add a new movie
- `PUT /movies/update/{movieTitle}` - Update a movie by title
- `DELETE /movies/{movieTitle}` - Delete a movie by title

### Theaters API
- `GET /theaters/all` - Get all theaters
- `GET /theaters/{id}` - Get theater by ID
- `POST /theaters` - Create a new theater

### Showtimes API
- `GET /showtimes/{id}` - Get showtime by ID
- `GET /showtimes/all` - Get all showtimes
- `POST /showtimes` - Add a new showtime
- `PUT /showtimes/update/{id}` - Update a showtime
- `DELETE /showtimes/{id}` - Delete a showtime

### Bookings API
- `POST /bookings` - Book a ticket

## Request/Response Examples

### Creating a Theater
**Request:**
```json
POST /theaters
{
  "name": "Grand Theater",
  "numberOfSeats": 150
}
```

**Response:**
```json
{
  "id": 1,
  "name": "Grand Theater",
  "numberOfSeats": 150
}
```

### Adding a Movie
**Request:**
```json
POST /movies
{
  "title": "Inception",
  "genre": "Sci-Fi",
  "duration": 148,
  "rating": 8.8,
  "releaseYear": 2010
}
```

**Response:**
```json
{
  "id": 1,
  "title": "Inception",
  "genre": "Sci-Fi",
  "duration": 148,
  "rating": 8.8,
  "releaseYear": 2010
}
```

### Creating a Showtime
**Request:**
```json
POST /showtimes
{
  "movieId": 1,
  "price": 12.50,
  "theater": "Grand Theater",
  "startTime": "2025-06-15T18:00:00Z",
  "endTime": "2025-06-15T20:30:00Z"
}
```

**Response:**
```json
{
  "id": 1,
  "movieId": 1,
  "price": 12.50,
  "theater": "Grand Theater",
  "startTime": "2025-06-15T18:00:00Z",
  "endTime": "2025-06-15T20:30:00Z"
}
```

### Booking a Ticket
**Request:**
```json
POST /bookings
{
  "showtimeId": 1,
  "seatNumber": 42,
  "userId": "84438967-f68f-4fa0-b620-0f08217e76af"
}
```

**Response:**
```json
{
  "bookingId": "d1a6423b-4469-4b00-8c5f-e3cfc42eacae"
}
```