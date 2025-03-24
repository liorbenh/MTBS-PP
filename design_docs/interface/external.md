# External API

This document outlines the REST API endpoints provided by the Popcorn Palace application.

## Authentication

All endpoints are currently open and do not require authentication.

## Movies API

| API Description | Endpoint | Method | Request Body | Response Status | Response Body |
|-----------------|----------|--------|--------------|-----------------|---------------|
| Get all movies | `/movies/all` | GET | - | 200 OK | `[{"id": 12345, "title": "Sample Movie Title 1", "genre": "Action", "duration": 120, "rating": 8.7, "releaseYear": 2025}, {"id": 67890, "title": "Sample Movie Title 2", "genre": "Comedy", "duration": 90, "rating": 7.5, "releaseYear": 2024}]` |
| Add a movie | `/movies` | POST | `{"title": "Sample Movie Title", "genre": "Action", "duration": 120, "rating": 8.7, "releaseYear": 2025}` | 200 OK | `{"id": 1, "title": "Sample Movie Title", "genre": "Action", "duration": 120, "rating": 8.7, "releaseYear": 2025}` |
| Update a movie | `/movies/update/{movieTitle}` | PUT | `{"title": "Sample Movie Title", "genre": "Action", "duration": 120, "rating": 8.7, "releaseYear": 2025}` | 200 OK | - |
| Delete a movie | `/movies/{movieTitle}` | DELETE | - | 200 OK | - |

## Showtimes API

| API Description | Endpoint | Method | Request Body | Response Status | Response Body |
|-----------------|----------|--------|--------------|-----------------|---------------|
| Get showtime by ID | `/showtimes/{showtimeId}` | GET | - | 200 OK | `{"id": 1, "price": 50.2, "movieId": 1, "theater": "Sample Theater", "startTime": "2025-02-14T11:47:46.125405Z", "endTime": "2025-02-14T14:47:46.125405Z"}` |
| Add a showtime | `/showtimes` | POST | `{"movieId": 1, "price": 20.2, "theater": "Sample Theater", "startTime": "2025-02-14T11:47:46.125405Z", "endTime": "2025-02-14T14:47:46.125405Z"}` | 200 OK | `{"id": 1, "price": 50.2, "movieId": 1, "theater": "Sample Theater", "startTime": "2025-02-14T11:47:46.125405Z", "endTime": "2025-02-14T14:47:46.125405Z"}` |
| Update a showtime | `/showtimes/update/{showtimeId}` | PUT | `{"movieId": 1, "price": 50.2, "theater": "Sample Theater", "startTime": "2025-02-14T11:47:46.125405Z", "endTime": "2025-02-14T14:47:46.125405Z"}` | 200 OK | - |
| Delete a showtime | `/showtimes/{showtimeId}` | DELETE | - | 200 OK | - |

## Theaters API

| API Description | Endpoint | Method | Request Body | Response Status | Response Body |
|-----------------|----------|--------|--------------|-----------------|---------------|
| Get all theaters | `/theaters/all` | GET | - | 200 OK | `[{"id": 1, "name": "Sample Theater 1", "numberOfSeats": 100}, {"id": 2, "name": "Sample Theater 2", "numberOfSeats": 150}]` |
| Get theater by ID | `/theaters/{id}` | GET | - | 200 OK | `{"id": 1, "name": "Sample Theater", "numberOfSeats": 100}` |
| Create a theater | `/theaters` | POST | `{"name": "New Theater", "numberOfSeats": 120}` | 200 OK | `{"id": 3, "name": "New Theater", "numberOfSeats": 120}` |

## Bookings API

| API Description | Endpoint | Method | Request Body | Response Status | Response Body |
|-----------------|----------|--------|--------------|-----------------|---------------|
| Book a ticket | `/bookings` | POST | `{"showtimeId": 1, "seatNumber": 15, "userId": "84438967-f68f-4fa0-b620-0f08217e76af"}` | 200 OK | `{"bookingId": "d1a6423b-4469-4b00-8c5f-e3cfc42eacae"}` |

## Error Responses

All API endpoints return standard HTTP error codes with descriptive messages:

| Error Scenario | Status Code | Response Body Example |
|----------------|-------------|------------------------|
| Resource not found | 404 Not Found | `{"message": "Movie with title 'Non-existent Movie' not found", "timestamp": "2023-06-15T10:30:45.123Z"}` |
| Invalid input data | 400 Bad Request | `{"message": "Invalid rating value. Must be between 0.0 and 10.0", "timestamp": "2023-06-15T10:30:45.123Z"}` |
| Duplicate resource | 409 Conflict | `{"message": "Movie with title 'Existing Movie' already exists", "timestamp": "2023-06-15T10:30:45.123Z"}` |
| Showtime overlap | 409 Conflict | `{"message": "Cannot create showtime. Overlaps with existing showtime in Theater 'Sample Theater'", "timestamp": "2023-06-15T10:30:45.123Z"}` |
| Seat already booked | 409 Conflict | `{"message": "Seat 15 for showtime 1 is already booked", "timestamp": "2023-06-15T10:30:45.123Z"}` |
| Unmapped URL | 404 Not Found | `{"message": "The requested resource '/invalid/path' was not found", "timestamp": "2023-06-15T10:30:45.123Z"}` |
| Method not allowed | 405 Method Not Allowed | `{"message": "Method 'POST' is not supported for this request path. Supported methods are: GET", "timestamp": "2023-06-15T10:30:45.123Z"}` |
| Server error | 500 Internal Server Error | `{"message": "An unexpected error occurred", "timestamp": "2023-06-15T10:30:45.123Z"}` |