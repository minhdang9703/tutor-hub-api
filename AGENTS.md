# TutorHub agent guide

## Project

- Java 21, Maven, Spring Boot 3.5, Spring Security, JPA/JdbcTemplate, PostgreSQL and Flyway.
- This is a modular monolith. Business packages live under `com.tutorhub.<module>`; shared code is in `common`.
- Implemented modules: `auth`, `user`, `catalog`, `parent`, `tutor`, `booking`, `review`, and `notification`.
- REST endpoints use `/api/v1`. Controllers call services; modules must not use another module's repository directly.

## Work rules

- Do not return JPA entities from HTTP APIs. Add request/response DTOs (normally records) and validate incoming DTOs with `@Valid`.
- Enforce role and resource ownership in the service layer using `CurrentUser`/`UserPrincipal`. Use the existing `ApiException` subclasses so `GlobalExceptionHandler` preserves the API error format.
- Keep schema changes additive in a new sequential Flyway migration under `src/main/resources/db/migration/`. Never edit `V1__init_schema.sql` or `V2__seed_reference_data.sql` after they have been applied.
- Keep money as `long` (VND) and instants as `Instant`/PostgreSQL `TIMESTAMPTZ`; do not use floating-point money or local timestamp types for bookings.
- Booking overlap is enforced by PostgreSQL's `ex_bookings_no_overlap` exclusion constraint. Retain database enforcement and translate its violation to the existing conflict response when modifying booking writes.
- Reuse the configured `FileStorage` abstraction for object storage. Do not commit `.env`, credentials, or generated `target/` output.

## Configuration and local run

1. Copy `.env.example` to `.env` and provide a real Base64-encoded 32-byte `JWT_SECRET`.
2. Start PostgreSQL, MinIO, and Mailpit: `docker compose up -d`.
3. Run the app: `mvn spring-boot:run -Dspring-boot.run.profiles=dev`.

Swagger is at `http://localhost:8080/swagger-ui.html`; Mailpit is at `http://localhost:8025`.

## Verification

- Run focused tests while changing a feature, then `mvn test` before handoff.
- Unit tests are ordinary JUnit tests. Integration tests extend `PostgresIntegrationTest` and use Testcontainers; they are skipped when Docker is unavailable.
- Prefer tests that exercise authorization, ownership, state transitions, and database constraints for new business rules.

## Planning references

- `CHECKPOINT.md` states what is implemented and what remains. It is the current delivery status.
- `tutor-hub.md` contains the broader product/API plan and schema rationale.
