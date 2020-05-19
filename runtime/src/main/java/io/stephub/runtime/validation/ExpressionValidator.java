package io.stephub.runtime.validation;

import io.stephub.expression.ExpressionEvaluator;
import io.stephub.expression.MatchResult;
import io.stephub.expression.impl.DefaultExpressionEvaluator;

import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public class ExpressionValidator implements ConstraintValidator<ExpressionValidator.Valid, String> {
    final ExpressionEvaluator evaluator = new DefaultExpressionEvaluator();

    @Override
    public boolean isValid(final String expressionStr, final ConstraintValidatorContext cvc) {
        final MatchResult match = this.evaluator.match(expressionStr);
        if (match.matches()) {
            return true;
        } else {
            cvc.buildConstraintViolationWithTemplate(match.getParseException().getMessage()).addConstraintViolation().disableDefaultConstraintViolation();
            return false;
        }
    }

    @Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.TYPE_USE})
    @Retention(RetentionPolicy.RUNTIME)
    @Constraint(validatedBy = {ExpressionValidator.class})
    public @interface Valid {
        String message() default "Invalid expression";

        Class<?>[] groups() default {};

        Class<? extends Payload>[] payload() default {};
    }
}
