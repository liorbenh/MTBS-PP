# Testing Guide
The test suite covers all layers of the application:

## Controller Tests
Verify API endpoints, request/response formats, and HTTP status codes.
- MovieControllerTest
- TheaterControllerTest
- ShowtimeControllerTest
- BookingControllerTest

## Service Tests
Verify business logic, data transformations, and error handling.
- MovieServiceTest
- TheaterServiceTest
- ShowtimeServiceTest
- BookingServiceTest
- SeatServiceTest
- ShowSeatServiceTest

## Repository Tests
Verify database operations and entity mappings.
- MovieRepositoryTest
- TheaterRepositoryTest
- ShowtimeRepositoryTest
- BookingRepositoryTest
- SeatRepositoryTest
- ShowSeatRepositoryTest

## Validation Tests
Verify input validation rules.
- MovieServiceValidationTest
- TheaterServiceValidationTest
- ShowtimeServiceValidationTest
- BookingServiceValidationTest

## Integration Tests
Verify end-to-end workflows.
- BookingWorkflowIntegrationTest
- ShowtimeManagementIntegrationTest
- ConcurrentBookingIntegrationTest
