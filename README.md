# AI-Assisted URL Shortener

Production-ready modular monolith URL shortener built with Java 21, Spring Boot 3, Gradle,
PostgreSQL, Redis, Spring Data JPA, Spring Validation, Actuator, SpringDoc OpenAPI, JUnit 5,
Mockito, and Testcontainers.

## Setup

Requirements:

- Java 21
- Docker Desktop
- Gradle 8.x, or an IDE with Gradle support

Start infrastructure:

```powershell
docker compose up -d postgres redis
```

Swagger UI is available at:

```text
http://localhost:8080/swagger-ui.html
```

Health check:

```text
http://localhost:8080/actuator/health
```

## Build

```powershell
gradle clean build
```

## Run

Run locally against Docker Compose services:

```powershell
gradle bootRun
```

Run the full containerized app:

```powershell
docker compose up --build
```

The application starts on:

```text
http://localhost:8080
```

## Test

```powershell
gradle test
```

Run Docker-backed integration tests:

```powershell
gradle integrationTest
```

Tests include:

- Unit tests with Mockito
- Controller tests with MockMvc
- Repository tests with PostgreSQL Testcontainers
- Integration tests with PostgreSQL and Redis Testcontainers

Docker Desktop must be running for Testcontainers-backed repository and integration tests. The
default `gradle test` task runs the fast non-container test suite; `gradle integrationTest` runs the
Docker-dependent test suite.

## API Examples

Create a short URL:

```powershell
Invoke-RestMethod `
  -Method Post `
  -Uri http://localhost:8080/api/v1/urls `
  -ContentType application/json `
  -Body '{"originalUrl":"https://www.schwab.com/"}'
```

Example response:

```json
{
  "originalUrl": "https://www.schwab.com/",
  "shortCode": "AbC123x",
  "shortUrl": "http://localhost:8080/AbC123x",
  "createdAt": "2026-08-03T08:00:00Z"
}
```

Redirect:

```powershell
curl.exe -i http://localhost:8080/AbC123x
```

Analytics:

```powershell
Invoke-RestMethod http://localhost:8080/api/v1/analytics/AbC123x
```

Health:

```powershell
Invoke-RestMethod http://localhost:8080/actuator/health
```

## Design Decisions

- Duplicate original URLs are detected by a unique PostgreSQL constraint and repository lookup.
- First-time creation returns `201 Created`; duplicates return `200 OK` with the existing short URL.
- Short codes are random Base62 values to avoid predictable enumeration.
- Redis stores `shortCode -> originalUrl` with a configurable TTL.
- Redirects read from Redis first and fall back to PostgreSQL on cache miss.
- Analytics are persisted in PostgreSQL with browser, device, referrer, and click timestamp.
- API errors are structured and handled globally without stack traces in client responses.
