package io.stephub.server.validation;

import io.stephub.expression.ExpressionEvaluator;
import io.stephub.expression.MatchResult;
import io.stephub.expression.impl.DefaultExpressionEvaluator;
import io.stephub.server.api.validation.IExpressionValidator;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintValidatorContext;

@Component
public class ExpressionValidator implements IExpressionValidator {
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

}
