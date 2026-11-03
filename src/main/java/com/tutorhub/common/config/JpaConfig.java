package com.tutorhub.common.config;

import java.util.Optional;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing
class JpaConfig {
    AuditorAware<Long> auditorAware() { return Optional::empty; }
}
