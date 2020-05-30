package io.stephub.runtime.validation;

import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Validates a string to be a regex.
 */
public class RegexValidator implements ConstraintValidator<RegexValidator.Regex, String> {
    private int minGroupCount;

    @Override
    public void initialize(final Regex ra) {
        this.minGroupCount = ra.minGroupCount();
    }

    @Override
    public boolean isValid(final String regexStr, final ConstraintValidatorContext cvc) {
        try {
            final Matcher matcher = Pattern.compile(regexStr).matcher("something");
            if (matcher.groupCount() < this.minGroupCount) {
                cvc.buildConstraintViolationWithTemplate("At least " + this.minGroupCount +
                        " capturing group" + (this.minGroupCount > 1 ? "s" : "") +
                        " expected").addConstraintViolation().disableDefaultConstraintViolation();
            }
            return true;
        } catch (final Exception e) {
            cvc.buildConstraintViolationWithTemplate(e.getMessage()).addConstraintViolation().disableDefaultConstraintViolation();
            return false;
        }
    }

    @Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.TYPE_USE})
    @Retention(RetentionPolicy.RUNTIME)
    @Constraint(validatedBy = {RegexValidator.class})
    public @interface Regex {
        int minGroupCount() default 0;

        String message() default "Invalid RegEx";

        Class<?>[] groups() default {};

        Class<? extends Payload>[] payload() default {};
    }
}
