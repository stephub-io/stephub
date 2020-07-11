package io.stephub.server.api.model.customsteps;

import io.stephub.expression.EvaluationContext;
import io.stephub.expression.ExpressionEvaluator;
import io.stephub.json.JsonBoolean;
import io.stephub.server.api.SessionExecutionContext;
import io.stephub.server.api.model.NestedStepResponse;
import io.stephub.server.api.validation.ValidExpression;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.validation.Errors;

import java.util.ArrayList;
import java.util.List;

import static io.stephub.json.Json.JsonType.BOOLEAN;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ConditionalStepDefinition extends StepDefinition {
    @ValidExpression
    private String conditionExpression;
    private final List<String> truthySteps = new ArrayList<>();
    private final List<String> faultySteps = new ArrayList<>();

    @Override
    public void validate(final String fieldPrefix, final Errors errors, final StepExecutionResolverWrapper stepExecutionResolver) {
        for (int i = 0; i < this.truthySteps.size(); i++) {
            if (stepExecutionResolver.resolveStepExecution(this.truthySteps.get(i)) == null) {
                errors.rejectValue(fieldPrefix + "truthySteps[" + i + "]", "msg.step.unknown", "Step definition not found");
            }
        }
        for (int i = 0; i < this.faultySteps.size(); i++) {
            if (stepExecutionResolver.resolveStepExecution(this.faultySteps.get(i)) == null) {
                errors.rejectValue(fieldPrefix + "faultySteps[" + i + "]", "msg.step.unknown", "Step definition not found");
            }
        }
        super.validate(fieldPrefix, errors, stepExecutionResolver);
    }

    @Override
    protected NestedStepResponse executeInternally(final SessionExecutionContext sessionExecutionContext, final EvaluationContext evaluationContext, final StepExecutionResolverWrapper stepExecutionResolver, final ExpressionEvaluator expressionEvaluator) {
        final JsonBoolean condition = (JsonBoolean) BOOLEAN.convertFrom(expressionEvaluator.evaluate(this.conditionExpression, evaluationContext));
        final NestedStepResponse.Context.ContextBuilder subResponses = NestedStepResponse.Context.
                builder();
        final List<String> steps;
        if (condition.isTrue()) {
            steps = this.truthySteps;
            subResponses.name("Truthy steps");
        } else {
            steps = this.faultySteps;
            subResponses.name("Faulty steps");
        }
        this.executeNestedSteps(sessionExecutionContext, evaluationContext, stepExecutionResolver, subResponses, steps);
        return NestedStepResponse.builder().subResponse(subResponses.build()).build();
    }


}
