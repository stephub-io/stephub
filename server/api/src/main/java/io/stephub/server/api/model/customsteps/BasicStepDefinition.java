package io.stephub.server.api.model.customsteps;

import io.stephub.expression.EvaluationContext;
import io.stephub.expression.ExpressionEvaluator;
import io.stephub.server.api.SessionExecutionContext;
import io.stephub.server.api.model.StepResponseContext;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.validation.Errors;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@NoArgsConstructor
@Getter
@Setter
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class BasicStepDefinition extends StepDefinition {
    @Singular
    private List<String> steps = new ArrayList<>();

    @Override
    public void validate(final String fieldPrefix, final Errors errors, final StepExecutionResolverWrapper stepExecutionResolver) {
        for (int i = 0; i < this.steps.size(); i++) {
            if (stepExecutionResolver.resolveStepExecution(this.steps.get(i)) == null) {
                errors.rejectValue(fieldPrefix + "steps[" + i + "]", "msg.step.unknown", "Step definition not found");
            }
        }
        super.validate(fieldPrefix, errors, stepExecutionResolver);
    }

    @Override
    protected void executeInternally(final SessionExecutionContext sessionExecutionContext, final EvaluationContext evaluationContext, final StepExecutionResolverWrapper stepExecutionResolver,
                                     final ExpressionEvaluator expressionEvaluator, final StepResponseContext responseContext) {
        this.executeNestedSteps(sessionExecutionContext, evaluationContext, stepExecutionResolver,
                responseContext.nested().group(Optional.empty()), this.steps);
    }

}
