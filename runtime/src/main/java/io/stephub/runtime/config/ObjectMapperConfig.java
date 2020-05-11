package io.stephub.runtime.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.stephub.json.jackson.ObjectMapperConfigurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
public class ObjectMapperConfig {
    @Autowired
    private ObjectMapper objectMapper;

    @PostConstruct
    public void setUp() {
        ObjectMapperConfigurer.configure(this.objectMapper);
    }
}
