package io.stephub.server.config;

import io.stephub.json.Json;
import io.stephub.json.schema.JsonSchema;
import io.stephub.provider.remote.RemoteProviderFactory;
import io.stephub.provider.util.spring.StepMethodAnnotationProcessor;
import io.stephub.providers.base.BaseProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.AnnotatedType;

@Configuration
@ComponentScan(basePackageClasses = {BaseProvider.class, RemoteProviderFactory.class})
public class ProvidersConfig {

    @Bean
    @Autowired
    public JsonSchemaStepMethodAnnotationProcessor stepMethodAnnotationProcessor(final ConfigurableListableBeanFactory beanFactory) {
        return new JsonSchemaStepMethodAnnotationProcessor(beanFactory);
    }

    public static class JsonSchemaStepMethodAnnotationProcessor extends StepMethodAnnotationProcessor {

        public JsonSchemaStepMethodAnnotationProcessor(final ConfigurableListableBeanFactory beanFactory) {
            super(beanFactory);
        }

        @Override
        protected Object wrapSchema(final AnnotatedType type) {
            return JsonSchema.ofType(Json.JsonType.valueOf((Class<?>) type.getType()));
        }
    }
}
