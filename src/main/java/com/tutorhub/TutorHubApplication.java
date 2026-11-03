package com.tutorhub;

import com.tutorhub.common.config.AppProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(AppProperties.class)
public class TutorHubApplication {
    public static void main(String[] args) {
        SpringApplication.run(TutorHubApplication.class, args);
    }
}
