package io.stephub.runtime.validation;

import io.stephub.json.schema.JsonSchema;
import io.stephub.provider.api.model.ProviderInfo;
import io.stephub.runtime.model.ProviderSpec;
import io.stephub.runtime.service.ProvidersFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Component
public class ProviderValidator implements ConstraintValidator<ProviderValidator.Valid, ProviderSpec> {

    @Autowired
    private ProvidersFacade providersFacade;

    @Override
    public boolean isValid(final ProviderSpec providerSpec, final ConstraintValidatorContext cvc) {
        try {
            ProviderInfo<JsonSchema> info = providersFacade.getProvider(providerSpec).getInfo();
            return true;
        } catch (Exception e) {
            cvc.buildConstraintViolationWithTemplate(e.getMessage()).addConstraintViolation().disableDefaultConstraintViolation();
            return false;
        }
    }

    @Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.TYPE_USE})
    @Retention(RetentionPolicy.RUNTIME)
    @Constraint(validatedBy = {ProviderValidator.class})
    public @interface Valid {
        String message() default "Not available provider";

        Class<?>[] groups() default {};

        Class<? extends Payload>[] payload() default {};
    }
}
