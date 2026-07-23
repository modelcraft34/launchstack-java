package com.launchstack.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Enables Spring Data JPA auditing so auditable entities can receive automatic timestamp updates.
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditConfig {
}
