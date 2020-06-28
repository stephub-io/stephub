package io.stephub.runtime.model.customsteps;

import io.stephub.expression.EvaluationContext;
import io.stephub.expression.ExpressionEvaluator;
import io.stephub.runtime.model.NestedStepResponse;
import io.stephub.runtime.service.SessionExecutionContext;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.validation.Errors;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Getter
@Setter
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class BasicStepDefinition extends StepDefinition {
    @Singular
    private List<String> instructions = new ArrayList<>();

    @Override
    public void validate(final String fieldPrefix, final Errors errors, final StepExecutionResolverWrapper stepExecutionResolver) {
        for (int i = 0; i < this.instructions.size(); i++) {
            if (stepExecutionResolver.resolveStepExecution(this.instructions.get(i)) == null) {
                errors.rejectValue(fieldPrefix + "instructions[" + i + "]", "msg.step.unknown", "Step definition not found");
            }
        }
        super.validate(fieldPrefix, errors, stepExecutionResolver);
    }

    @Override
    protected NestedStepResponse executeInternally(final SessionExecutionContext sessionExecutionContext, final EvaluationContext evaluationContext, final StepExecutionResolverWrapper stepExecutionResolver, final ExpressionEvaluator expressionEvaluator) {
        final NestedStepResponse.Context.ContextBuilder subResponses = NestedStepResponse.Context.
                builder();
        this.executeNestedInstructions(sessionExecutionContext, evaluationContext, stepExecutionResolver, subResponses, this.instructions);
        return NestedStepResponse.builder().subResponse(subResponses.build()).build();
    }

}
