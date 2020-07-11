package io.stephub.server.validation;

import io.stephub.json.schema.JsonSchema;
import io.stephub.provider.api.model.ProviderInfo;
import io.stephub.server.api.model.ProviderSpec;
import io.stephub.server.api.validation.IProviderValidator;
import io.stephub.server.service.ProvidersFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintValidatorContext;

@Component
public class ProviderValidator implements IProviderValidator {

    @Autowired
    private ProvidersFacade providersFacade;

    @Override
    public boolean isValid(final ProviderSpec providerSpec, final ConstraintValidatorContext cvc) {
        try {
            final ProviderInfo<JsonSchema> info = this.providersFacade.getProvider(providerSpec).getInfo();
            return true;
        } catch (final Exception e) {
            cvc.buildConstraintViolationWithTemplate(e.getMessage()).addConstraintViolation().disableDefaultConstraintViolation();
            return false;
        }
    }
}
