package com.company.urlshortener.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Configures the generated OpenAPI document. */
@Configuration
public class OpenApiConfig {

  /** Returns API metadata used by Swagger UI. */
  @Bean
  public OpenAPI openAPI() {
    return new OpenAPI()
        .info(
            new Info()
                .title("AI-Assisted URL Shortener")
                .version("1.0.0")
                .description("Production-ready modular monolith URL shortener API."));
  }
}
