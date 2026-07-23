package com.launchstack.config;

import com.launchstack.auth.service.AuthService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuthSeedConfig {

    @Bean
    public ApplicationRunner authSeedRunner(
            AuthService authService,
            @Value("${SEED_ADMIN_EMAIL:}") String seedAdminEmail,
            @Value("${SEED_ADMIN_PASSWORD:}") String seedAdminPassword) {
        return args -> authService.seedRolesAndAdmin(seedAdminEmail, seedAdminPassword);
    }
}
