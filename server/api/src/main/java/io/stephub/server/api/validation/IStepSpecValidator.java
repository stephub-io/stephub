package io.stephub.server.api.validation;

import io.stephub.json.schema.JsonSchema;
import io.stephub.provider.api.model.spec.StepSpec;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintValidator;

@Component
public interface IStepSpecValidator extends ConstraintValidator<ValidStepSpec, StepSpec<JsonSchema>> {

}
