package io.stephub.server.api.validation;

import io.stephub.server.api.model.ProviderSpec;

import javax.validation.ConstraintValidator;

public interface IProviderValidator extends ConstraintValidator<ValidProviderSpec, ProviderSpec> {

}
