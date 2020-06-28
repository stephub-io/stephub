package io.stephub.runtime.model.customsteps;

import io.stephub.expression.EvaluationContext;
import io.stephub.expression.ExpressionEvaluator;
import io.stephub.json.JsonBoolean;
import io.stephub.runtime.model.NestedStepResponse;
import io.stephub.runtime.service.SessionExecutionContext;
import io.stephub.runtime.validation.ExpressionValidator;
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
    @ExpressionValidator.Valid
    private String conditionExpression;
    private final List<String> truthyInstructions = new ArrayList<>();
    private final List<String> faultyInstructions = new ArrayList<>();

    @Override
    public void validate(final String fieldPrefix, final Errors errors, final StepExecutionResolverWrapper stepExecutionResolver) {
        for (int i = 0; i < this.truthyInstructions.size(); i++) {
            if (stepExecutionResolver.resolveStepExecution(this.truthyInstructions.get(i)) == null) {
                errors.rejectValue(fieldPrefix + "truthyInstructions[" + i + "]", "msg.step.unknown", "Step definition not found");
            }
        }
        for (int i = 0; i < this.faultyInstructions.size(); i++) {
            if (stepExecutionResolver.resolveStepExecution(this.faultyInstructions.get(i)) == null) {
                errors.rejectValue(fieldPrefix + "faultyInstructions[" + i + "]", "msg.step.unknown", "Step definition not found");
            }
        }
        super.validate(fieldPrefix, errors, stepExecutionResolver);
    }

    @Override
    protected NestedStepResponse executeInternally(final SessionExecutionContext sessionExecutionContext, final EvaluationContext evaluationContext, final StepExecutionResolverWrapper stepExecutionResolver, final ExpressionEvaluator expressionEvaluator) {
        final JsonBoolean condition = (JsonBoolean) BOOLEAN.convertFrom(expressionEvaluator.evaluate(this.conditionExpression, evaluationContext));
        final NestedStepResponse.Context.ContextBuilder subResponses = NestedStepResponse.Context.
                builder();
        final List<String> instructions;
        if (condition.isTrue()) {
            instructions = this.truthyInstructions;
            subResponses.name("Truthy instructions");
        } else {
            instructions = this.faultyInstructions;
            subResponses.name("Faulty instructions");
        }
        this.executeNestedInstructions(sessionExecutionContext, evaluationContext, stepExecutionResolver, subResponses, instructions);
        return NestedStepResponse.builder().subResponse(subResponses.build()).build();
    }


}
