package io.stephub.runtime.validation;

import io.stephub.json.schema.JsonSchema;
import io.stephub.provider.api.model.spec.PatternType;
import io.stephub.provider.api.model.spec.StepSpec;

import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public class StepSpecValidator implements ConstraintValidator<StepSpecValidator.Valid, StepSpec<JsonSchema>>  {

    @Override
    public boolean isValid(StepSpec<JsonSchema> spec, ConstraintValidatorContext cvc) {
        if (spec.getPatternType()!= PatternType.SIMPLE) {
            cvc.buildConstraintViolationWithTemplate("Only pattern type 'simple' is supported").addConstraintViolation().disableDefaultConstraintViolation();
            return false;
        }
        return false;
    }

    @Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.TYPE_USE})
    @Retention(RetentionPolicy.RUNTIME)
    @Constraint(validatedBy = {StepSpecValidator.class})
    public @interface Valid {
        String message() default "Invalid step spec";

        Class<?>[] groups() default {};

        Class<? extends Payload>[] payload() default {};
    }
}
