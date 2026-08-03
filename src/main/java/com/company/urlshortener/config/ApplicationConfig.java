package com.company.urlshortener.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/** Enables application-wide Spring infrastructure. */
@Configuration
@EnableJpaAuditing
@EnableConfigurationProperties(ApplicationProperties.class)
public class ApplicationConfig {}
