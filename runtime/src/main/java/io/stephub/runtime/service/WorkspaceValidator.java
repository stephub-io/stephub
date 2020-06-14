package io.stephub.runtime.service;

import io.stephub.runtime.model.Workspace;
import io.stephub.runtime.model.customsteps.Step;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;

import java.util.List;

@Service
public class WorkspaceValidator {

    @Autowired
    private SmartValidator validator;

    @Autowired
    private StepExecutionResolver stepExecutionResolver;

    public void validate(final Workspace workspace) {
        final BeanPropertyBindingResult result = new BeanPropertyBindingResult(workspace, "workspace");
        this.validator.validate(workspace, result);
        this.validateSteps(result, workspace, workspace.getSteps());
        if (result.hasErrors()) {
            workspace.setErrors(result.getAllErrors());
        } else {
            workspace.setErrors(null);
        }
    }

    private void validateSteps(final Errors errors, final Workspace workspace, final List<Step> steps) {
        int i = 0;
        for (final Step step : steps) {
            step.validate("steps[" + i + "].", errors, (instruction) ->
                    this.stepExecutionResolver.resolveStepExecution(
                            instruction,
                            workspace
                    ));
            i++;
        }
    }
}
