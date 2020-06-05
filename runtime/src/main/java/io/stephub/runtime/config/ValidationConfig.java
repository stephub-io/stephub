package io.stephub.runtime.config;

import io.stephub.json.schema.JsonSchema;
import io.stephub.runtime.validation.JsonSchemaValidator;
import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.cfg.ConstraintMapping;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import javax.validation.Validation;
import javax.validation.bootstrap.ProviderSpecificBootstrap;

@Configuration
public class ValidationConfig {

    @Bean
    public LocalValidatorFactoryBean validator() {
        final ProviderSpecificBootstrap<HibernateValidatorConfiguration> hibernateValidatorConfigurationProviderSpecificBootstrap = Validation.byProvider(HibernateValidator.class);

        final LocalValidatorFactoryBean localValidatorFactoryBean = new LocalValidatorFactoryBean() {
            @Override
            protected void postProcessConfiguration(final javax.validation.Configuration<?> configuration) {
                super.postProcessConfiguration(configuration);

                if (!(configuration instanceof HibernateValidatorConfiguration)) {
                    return;
                }
                final HibernateValidatorConfiguration hibernateValidatorConfiguration = (HibernateValidatorConfiguration) configuration;
                ValidationConfig.this.addJsonSchemaConstraintMapping(hibernateValidatorConfiguration);
            }
        };
        return localValidatorFactoryBean;
    }

    private void addJsonSchemaConstraintMapping(final HibernateValidatorConfiguration config) {
        final ConstraintMapping mapping = config.createConstraintMapping();
        mapping.type(JsonSchema.class).constraint(new JsonSchemaValidator.ValidSchemaDef());
        config.addMapping(mapping);
    }
}
