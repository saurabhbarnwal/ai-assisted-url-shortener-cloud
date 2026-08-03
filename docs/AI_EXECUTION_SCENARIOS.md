# AI-Assisted Engineering Execution Scenarios

This document records the delivery phases used for the Java/Spring Boot URL shortener.

## Phase 1: Foundation

Files created:

- `settings.gradle`
- `build.gradle`
- `src/main/resources/application.yml`
- `src/main/java/com/company/urlshortener/UrlShortenerApplication.java`
- `src/main/java/com/company/urlshortener/config/*`
- `src/main/java/com/company/urlshortener/entity/*`

Design decisions:

- Use Java 21 and Spring Boot 3.x with a single modular monolith deployment unit.
- Use Spring Data JPA entities for `urls` and `analytics` matching the requested database model.
- Enable Spring Actuator health and SpringDoc Swagger UI.

Approval required:

- None.

## Phase 2: API and Business Behavior

Files created:

- `controller/UrlController.java`
- `service/UrlShortenerService.java`
- `service/impl/UrlShortenerServiceImpl.java`
- `analytics/*`
- `cache/UrlCache.java`
- `repository/*`
- `mapper/*`
- `validation/UrlValidator.java`
- `util/*`
- `exception/*`
- `dto/*`

Design decisions:

- `POST /api/v1/urls` returns `201 Created` for a new URL and `200 OK` for duplicate original URLs.
- Redis stores `shortCode -> originalUrl`; redirects read Redis first and fall back to PostgreSQL.
- Click analytics are persisted after each successful redirect.
- URL validation accepts only `http` and `https`, requires a host, and rejects embedded credentials.

Approval required:

- None.

## Phase 3: Tests

Files created:

- `src/test/java/com/company/urlshortener/service/UrlShortenerServiceImplTest.java`
- `src/test/java/com/company/urlshortener/controller/UrlControllerTest.java`
- `src/test/java/com/company/urlshortener/repository/UrlRepositoryTest.java`
- `src/test/java/com/company/urlshortener/integration/UrlShortenerIntegrationTest.java`

Design decisions:

- Use Mockito for service unit tests.
- Use MockMvc for controller tests.
- Use PostgreSQL Testcontainers for repository behavior.
- Use PostgreSQL and Redis Testcontainers for the full create, redirect, and analytics flow.

Approval required:

- None.

## Phase 4: Operations and Documentation

Files created:

- `Dockerfile`
- `docker-compose.yml`
- `README.md`
- `docs/ARCHITECTURE.md`
- `docs/OPENAPI.yaml`

Design decisions:

- Docker Compose runs PostgreSQL, Redis, and the Spring Boot app.
- Hibernate `ddl-auto=update` is used for assignment-friendly setup.
- A production migration tool such as Flyway or Liquibase should replace Hibernate schema updates
  in a stricter release environment.

Approval required:

- None.
