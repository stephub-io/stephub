package io.stephub.server.api.validation;

import io.stephub.server.api.model.ProviderSpec;

import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public interface IProviderValidator extends ConstraintValidator<IProviderValidator.Valid, ProviderSpec> {

    @Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.TYPE_USE})
    @Retention(RetentionPolicy.RUNTIME)
    @Constraint(validatedBy = {IProviderValidator.class})
    public @interface Valid {
        String message() default "Not available provider";

        Class<?>[] groups() default {};

        Class<? extends Payload>[] payload() default {};
    }
}
