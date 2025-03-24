# Architecture
A multi-layered architecture (5-Tier Architecture) will be used, 
which is commonly used in Spring Boot applications, to ensure separation of concerns and maintainability.

### Layers and Their Responsibilities

| Layer               | Role                                      | Example Annotation / Class |
|---------------------|-----------------------------------------|----------------------------|
| **Controller Layer** | Handles API requests, HTTP logic       | `@RestController`          |
| **Service Layer**   | Business logic, processing data         | `@Service`                 |
| **Repository Layer** | Handles database CRUD operations       | `@Repository`              |
| **Entity Layer**    | Represents database structure           | `@Entity`                  |
| **DTO Layer** | Prevents exposing database structure in APIs | `MovieDTO`              |
DTO = Data Transfer Object

### Why Use This Pattern?
- **Better Maintainability**: Changes in one layer don’t affect others.
- **Scalability**: Easily expand functionality and integrate microservices.
- **Security**: Prevents direct exposure of the database structure using DTOs.
- **Testability**: Each layer can be independently tested to ensure reliability and correctness.

This architecture ensures a **clean separation** between different responsibilities 
in the application, making it easier to manage, scale, and test.

### Package Structure

```
popcorn_palace
├── controller     # REST API endpoints
├── service        # Business logic 
├── repository     # Data access objects
├── entity         # JPA entities
├── dto            # Data transfer objects
├── constants      # Shared constant values
└── exception      # Custom exceptions
```
