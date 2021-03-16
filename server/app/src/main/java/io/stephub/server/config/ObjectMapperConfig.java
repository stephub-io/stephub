package io.stephub.server.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.stephub.json.jackson.ObjectMapperConfigurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.ObjectError;

import javax.annotation.PostConstruct;

@Configuration
public class ObjectMapperConfig {
    @Autowired
    private ObjectMapper objectMapper;

    @PostConstruct
    public void setUp() {
        ObjectMapperConfigurer.configure(this.objectMapper);
        this.objectMapper.addMixIn(ObjectError.class, JsonObjectError.class);
        this.objectMapper.enable(MapperFeature.DEFAULT_VIEW_INCLUSION);
    }

    @JsonIgnoreProperties({"bindingFailure", "objectName"})
    public static class JsonObjectError extends ObjectError {

        public JsonObjectError(final String objectName, final String defaultMessage) {
            super(objectName, defaultMessage);
        }

        @Override
        @JsonIgnore
        public String getCode() {
            return super.getCode();
        }

        @Override
        @JsonIgnore
        public String[] getCodes() {
            return super.getCodes();
        }

        @Override
        @JsonIgnore
        public Object[] getArguments() {
            return super.getArguments();
        }

        @Override
        @JsonProperty("message")
        public String getDefaultMessage() {
            return super.getDefaultMessage();
        }
    }
}
