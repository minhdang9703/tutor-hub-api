package com.tutorhub.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class OpenApiConfig {
    @Bean OpenAPI tutorHubOpenApi() { return new OpenAPI().info(new Info().title("TutorHub API").version("v1").description("Tutor-parent matching platform API. Authentication uses a Bearer JWT.")); }
}
