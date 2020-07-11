package io.stephub.server.validation;

import io.stephub.server.api.validation.IRegexValidator;
import io.stephub.server.api.validation.ValidRegex;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintValidatorContext;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Validates a string to be a regex.
 */
@Component
public class RegexValidator implements IRegexValidator {
    private int minGroupCount;

    @Override
    public void initialize(final ValidRegex ra) {
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

}
