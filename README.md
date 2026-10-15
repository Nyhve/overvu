# Personal Life OS

Persönliche Life-Management-Anwendung. Aktuell in Phase 0 (Project Setup).

## Tech Stack
- Java 21, Spring Boot 4.1
- PostgreSQL, Flyway
- Docker Compose (lokale Datenbank)

## Lokale Entwicklung

1. `docker compose up -d`
2. `cd backend && ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev`
3. App läuft auf http://localhost:8080