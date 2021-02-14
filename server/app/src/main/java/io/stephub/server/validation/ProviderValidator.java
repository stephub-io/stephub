package io.stephub.server.validation;

import io.stephub.provider.api.ProviderException;
import io.stephub.server.api.model.ProviderSpec;
import io.stephub.server.api.validation.IProviderValidator;
import io.stephub.server.service.ProvidersFacade;
import org.apache.commons.lang3.StringUtils;
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
            if (StringUtils.isNotBlank(providerSpec.getName())) {
                this.providersFacade.getProvider(providerSpec).getInfo();
            }
            return true;
        } catch (final ProviderException e) {
            cvc.buildConstraintViolationWithTemplate(e.getMessage()).addConstraintViolation().disableDefaultConstraintViolation();
            return false;
        } catch (final Exception e) {
            cvc.buildConstraintViolationWithTemplate("Failed to resolve provider info: " + e.getMessage()).addConstraintViolation().disableDefaultConstraintViolation();
            return false;
        }
    }
}
