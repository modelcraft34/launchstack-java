package com.launchstack.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configures the base OpenAPI metadata and Swagger UI exposure for the backend.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI launchStackOpenApi() {
        return new OpenAPI()
                .components(new Components().addSecuritySchemes("bearerAuth",
                        new SecurityScheme()
                                .name("bearerAuth")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")))
                .info(new Info()
                        .title("LaunchStack Java API")
                        .description("Backend API for the LaunchStack Java starter kit.")
                        .version("v0.2"));
    }
}
