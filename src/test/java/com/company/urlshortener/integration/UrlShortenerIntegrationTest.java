package com.company.urlshortener.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.company.urlshortener.dto.AnalyticsResponse;
import com.company.urlshortener.dto.CreateUrlRequest;
import com.company.urlshortener.dto.CreateUrlResponse;
import java.net.URI;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/** End-to-end API tests with PostgreSQL and Redis Testcontainers. */
@Tag("integration")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers(disabledWithoutDocker = true)
class UrlShortenerIntegrationTest {

  @Container
  private static final PostgreSQLContainer<?> POSTGRESQL_CONTAINER =
      new PostgreSQLContainer<>("postgres:16-alpine")
          .withDatabaseName("urlshortener_test")
          .withUsername("urlshortener")
          .withPassword("urlshortener");

  @Container
  private static final GenericContainer<?> REDIS_CONTAINER =
      new GenericContainer<>("redis:7-alpine").withExposedPorts(6379);

  private final TestRestTemplate restTemplate;

  @Autowired
  UrlShortenerIntegrationTest(TestRestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  /** Registers container-backed Spring properties. */
  @DynamicPropertySource
  static void registerContainerProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", POSTGRESQL_CONTAINER::getJdbcUrl);
    registry.add("spring.datasource.username", POSTGRESQL_CONTAINER::getUsername);
    registry.add("spring.datasource.password", POSTGRESQL_CONTAINER::getPassword);
    registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    registry.add("spring.data.redis.host", REDIS_CONTAINER::getHost);
    registry.add("spring.data.redis.port", REDIS_CONTAINER::getFirstMappedPort);
  }

  /** Creates, redirects, and reads analytics through real HTTP endpoints. */
  @Test
  void createRedirectAndAnalyticsFlow() {
    ResponseEntity<CreateUrlResponse> createResponse =
        restTemplate.postForEntity(
            "/api/v1/urls",
            new CreateUrlRequest("https://example.com/integration"),
            CreateUrlResponse.class);

    assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    CreateUrlResponse createdUrl = createResponse.getBody();
    assertThat(createdUrl).isNotNull();

    HttpHeaders headers = new HttpHeaders();
    headers.set(HttpHeaders.USER_AGENT, "Mozilla/5.0 Chrome/120");
    ResponseEntity<Void> redirectResponse =
        restTemplate.exchange(
            "/" + createdUrl.shortCode(), HttpMethod.GET, new HttpEntity<>(headers), Void.class);

    assertThat(redirectResponse.getStatusCode()).isEqualTo(HttpStatus.FOUND);
    assertThat(redirectResponse.getHeaders().getLocation())
        .isEqualTo(URI.create("https://example.com/integration"));

    ResponseEntity<AnalyticsResponse> analyticsResponse =
        restTemplate.getForEntity(
            "/api/v1/analytics/" + createdUrl.shortCode(), AnalyticsResponse.class);

    assertThat(analyticsResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(analyticsResponse.getBody()).isNotNull();
    assertThat(analyticsResponse.getBody().totalClicks()).isEqualTo(1);
  }
}
