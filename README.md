# Mentoring Platform

REST API connecting students and mentors: lesson booking, completion confirmation, and mentor reviews with aggregated ratings.

## Tech Stack

- **Java 21**, **Spring Boot 4.1.0**
- **Spring Security** (JWT) + **Spring Data JPA**
- **PostgreSQL** + **Flyway** (schema migrations)
- **Apache Kafka** (event-driven notifications on lesson completion)
- **Testcontainers** (integration tests against real Postgres/Kafka)
- **Docker / Docker Compose**
- **GitHub Actions** (CI)
- **springdoc-openapi** (Swagger UI)

## Features

- Registration/login with role selection (Student / Mentor), JWT-based auth
- Mentor listing with pagination and specialization filter
- Mentor profile editing (bio, specialization, price per hour)
- Lesson booking with overlap detection and concurrency protection
- Lesson status transitions (scheduled → completed/cancelled)
- Reviews with automatically recalculated mentor rating
- Kafka event on lesson completion → asynchronously flags the lesson for a review reminder

## Architecture Notes

- Package-by-feature structure: `auth`, `user`, `mentor`, `student`, `lesson`, `review`, `common`
- Pessimistic locking on mentor rows during booking and review creation to prevent race conditions
- All `@ManyToOne`/`@OneToOne` associations are lazy-loaded, with `JOIN FETCH` on paginated queries to avoid N+1
- Mentor rating is recalculated via SQL aggregation (`AVG`/`COUNT`) on each review, not stored incrementally
- Lesson completion publishes a Kafka event; the consumer updates a `reviewReminderSent` flag via an atomic `UPDATE` query

## Running Locally

**Prerequisites:** Docker, Java 21 (only if running outside Docker)

```bash
git clone <repo-url>
cd mentoring-platform
cp .env.example .env   # fill in JWT_SECRET, DB credentials
docker compose up --build
```

The app will be available at `http://localhost:8080`, Swagger UI at `http://localhost:8080/swagger-ui/index.html`.

### Running without Docker

Start Postgres and Kafka locally (or via `docker compose up postgres kafka`), then run the app with the `local` Spring profile active — the app defaults to it automatically.

## Testing

```bash
mvn test
```

Unit tests use Mockito; integration tests spin up real Postgres/Kafka containers via Testcontainers.

## Known Limitations

- **Kafka publish happens before the enclosing transaction commits** (Spring commits only after the `@Transactional` method returns). If the method fails and rolls back after the event was already published, the database and Kafka can end up inconsistent. A transactional outbox pattern is the standard solution to this, but is out of scope for this project.