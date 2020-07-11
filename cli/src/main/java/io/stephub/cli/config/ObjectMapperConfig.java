package io.stephub.cli.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static io.stephub.json.jackson.ObjectMapperConfigurer.createObjectMapper;

@Configuration
public class ObjectMapperConfig {
    @Bean
    public ObjectMapper objectMapper() {
        return createObjectMapper();
    }
}
