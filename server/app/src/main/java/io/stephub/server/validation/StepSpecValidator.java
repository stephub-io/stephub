package io.stephub.server.validation;

import io.stephub.json.schema.JsonSchema;
import io.stephub.provider.api.model.spec.ArgumentSpec;
import io.stephub.provider.api.model.spec.PatternType;
import io.stephub.provider.api.model.spec.StepSpec;
import io.stephub.server.api.model.GherkinPreferences;
import io.stephub.server.api.validation.IStepSpecValidator;
import io.stephub.server.service.SimplePatternExtractor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintValidatorContext;
import java.util.HashMap;
import java.util.Map;

@Component
public class StepSpecValidator implements IStepSpecValidator {

    @Autowired
    private SimplePatternExtractor simplePatternExtractor;

    @Override
    public boolean isValid(final StepSpec<JsonSchema> spec, final ConstraintValidatorContext cvc) {
        cvc.disableDefaultConstraintViolation();
        if (spec.getPatternType() != PatternType.SIMPLE) {
            cvc.buildConstraintViolationWithTemplate("Only pattern type 'simple' is supported").addConstraintViolation();
            return false;
        }
        if (StringUtils.isEmpty(spec.getPattern())) {
            cvc.buildConstraintViolationWithTemplate("Pattern string required").addConstraintViolation();
            return false;
        }
        if (spec.getArguments() != null) {
            final SimplePatternExtractor.Extraction extractedPattern = this.simplePatternExtractor.extract(new GherkinPreferences(), spec.getPattern(), false);
            final Map<String, ArgumentSpec<JsonSchema>> arguments = new HashMap<>();
            spec.getArguments().forEach(a -> arguments.put(a.getName(), a));
            for (final ArgumentSpec<JsonSchema> patternArg : extractedPattern.getArguments()) {
                arguments.remove(patternArg.getName());
            }
            if (!arguments.isEmpty()) {
                arguments.forEach((name, a) ->
                        cvc.buildConstraintViolationWithTemplate("Specified argument '" + name + "' isn't defined in the step pattern").addConstraintViolation()
                );
                return false;
            }
        }
        if (spec.getPayload() == StepSpec.PayloadType.DATA_TABLE) {
            if (spec.getDataTable() == null) {
                cvc.buildConstraintViolationWithTemplate("Specified payload 'data_table', but no data table specification passed").addConstraintViolation();
                return false;
            }
        }
        return true;
    }

}
