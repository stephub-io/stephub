package io.stephub.cli.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.stephub.json.jackson.ObjectMapperConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import static io.stephub.json.jackson.ObjectMapperConfigurer.createObjectMapper;

@Configuration
public class ObjectMapperConfig {
    public static final String YAML = "yamlObjectMapper";

    @Primary
    @Bean
    public ObjectMapper objectMapper() {
        return createObjectMapper();
    }

    @Bean(value = YAML)
    public ObjectMapper yamlObjectMapper() {
        final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
        ObjectMapperConfigurer.configure(yamlMapper);
        return yamlMapper;
    }
}
