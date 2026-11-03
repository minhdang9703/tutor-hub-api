package com.tutorhub.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
class WebConfig implements WebMvcConfigurer {
    private final String allowedOrigins;
    WebConfig(@Value("${app.cors.allowed-origins}") String allowedOrigins) { this.allowedOrigins = allowedOrigins; }
    @Override public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**").allowedOrigins(allowedOrigins.split(",")).allowedMethods("GET", "POST", "PATCH", "PUT", "DELETE");
    }
}
