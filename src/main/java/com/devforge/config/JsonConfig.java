package com.devforge.config;

import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.cfg.DateTimeFeature;

@Configuration
public class JsonConfig {

    @Bean
    JsonMapperBuilderCustomizer isoDates() {
        return builder -> builder.disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS);
    }
}
