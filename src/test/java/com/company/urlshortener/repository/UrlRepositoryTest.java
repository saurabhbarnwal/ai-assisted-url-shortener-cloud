package com.company.urlshortener.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.company.urlshortener.entity.AnalyticsEntity;
import com.company.urlshortener.entity.UrlEntity;
import java.time.Instant;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/** Repository tests backed by PostgreSQL Testcontainers. */
@Tag("integration")
@DataJpaTest
@Testcontainers(disabledWithoutDocker = true)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UrlRepositoryTest {

  @Container
  private static final PostgreSQLContainer<?> POSTGRESQL_CONTAINER =
      new PostgreSQLContainer<>("postgres:16-alpine")
          .withDatabaseName("urlshortener_test")
          .withUsername("urlshortener")
          .withPassword("urlshortener");

  private final UrlRepository urlRepository;
  private final AnalyticsRepository analyticsRepository;

  /** Enables auditing for repository tests. */
  @TestConfiguration
  @EnableJpaAuditing
  static class JpaAuditingTestConfiguration {}

  @Autowired
  UrlRepositoryTest(UrlRepository urlRepository, AnalyticsRepository analyticsRepository) {
    this.urlRepository = urlRepository;
    this.analyticsRepository = analyticsRepository;
  }

  /** Registers PostgreSQL connection properties. */
  @DynamicPropertySource
  static void registerDatabaseProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", POSTGRESQL_CONTAINER::getJdbcUrl);
    registry.add("spring.datasource.username", POSTGRESQL_CONTAINER::getUsername);
    registry.add("spring.datasource.password", POSTGRESQL_CONTAINER::getPassword);
    registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
  }

  /** Persists and finds URLs by short code and original URL. */
  @Test
  void persistsAndFindsUrlEntity() {
    UrlEntity savedUrl = urlRepository.saveAndFlush(new UrlEntity("https://example.com", "AbC123x"));

    assertThat(urlRepository.findByShortCode("AbC123x")).contains(savedUrl);
    assertThat(urlRepository.findByOriginalUrl("https://example.com")).contains(savedUrl);
    assertThat(savedUrl.getCreatedAt()).isNotNull();
  }

  /** Persists analytics rows and returns recent clicks newest first. */
  @Test
  void persistsAnalyticsEvents() {
    UrlEntity savedUrl = urlRepository.saveAndFlush(new UrlEntity("https://example.com", "AbC123x"));
    analyticsRepository.save(
        new AnalyticsEntity(savedUrl, Instant.parse("2026-01-01T00:00:00Z"), "Chrome", "Desktop", null));
    analyticsRepository.save(
        new AnalyticsEntity(savedUrl, Instant.parse("2026-01-02T00:00:00Z"), "Firefox", "Mobile", "ref"));
    analyticsRepository.flush();

    assertThat(analyticsRepository.countByUrl(savedUrl)).isEqualTo(2);
    assertThat(
            analyticsRepository.findByUrlOrderByClickTimestampDesc(savedUrl, PageRequest.of(0, 1)))
        .extracting(AnalyticsEntity::getBrowser)
        .containsExactly("Firefox");
  }
}
