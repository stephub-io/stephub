package io.stephub.runtime.validation;

import io.stephub.json.schema.JsonSchema;
import org.hibernate.validator.cfg.ConstraintDef;

import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Validates JSON schema.
 */
public class JsonSchemaValidator implements ConstraintValidator<JsonSchemaValidator.ValidSchema, JsonSchema> {

    @Override
    public boolean isValid(final JsonSchema value, final ConstraintValidatorContext context) {
        final JsonSchema.SchemaValidity validate = value.validate();
        if (validate.isValid()) {
            return true;
        }
        if (!validate.getErrors().isEmpty()) {
            validate.getErrors().forEach(e ->
                    context.buildConstraintViolationWithTemplate(e).addConstraintViolation()
            );
            context.disableDefaultConstraintViolation();
        }
        return false;
    }

    public static class ValidSchemaDef extends ConstraintDef<JsonSchemaValidator.ValidSchemaDef, JsonSchemaValidator.ValidSchema> {

        public ValidSchemaDef() {
            super(JsonSchemaValidator.ValidSchema.class);
        }

    }

    @Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.TYPE_USE})
    @Retention(RetentionPolicy.RUNTIME)
    @Constraint(validatedBy = {JsonSchemaValidator.class})
    public @interface ValidSchema {
        int minGroupCount() default 0;

        String message() default "Invalid JSON schema";

        Class<?>[] groups() default {};

        Class<? extends Payload>[] payload() default {};
    }
}
