package com.launchstack.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI launchStackOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("LaunchStack Java API")
                        .description("Backend foundation for the LaunchStack Java starter kit.")
                        .version("v0.2"));
    }
}
