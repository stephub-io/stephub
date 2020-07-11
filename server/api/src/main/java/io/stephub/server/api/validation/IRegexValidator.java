package io.stephub.server.api.validation;

import javax.validation.ConstraintValidator;

/**
 * Validates a string to be a regex.
 */
public interface IRegexValidator extends ConstraintValidator<ValidRegex, String> {

}
