package io.stephub.runtime.model.customsteps;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.stephub.expression.EvaluationContext;
import io.stephub.expression.ExpressionEvaluator;
import io.stephub.json.Json;
import io.stephub.json.schema.JsonSchema;
import io.stephub.provider.api.model.StepRequest;
import io.stephub.provider.api.model.StepResponse;
import io.stephub.provider.api.model.spec.PatternType;
import io.stephub.provider.api.model.spec.StepSpec;
import io.stephub.runtime.model.NestedStepResponse;
import io.stephub.runtime.service.SessionExecutionContext;
import io.stephub.runtime.service.SessionService;
import io.stephub.runtime.service.StepExecution;
import lombok.*;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.Errors;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@SuperBuilder
@EqualsAndHashCode
@ToString
@Getter
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        defaultImpl = BasicStep.class,
        property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = BasicStep.class, name = "basic"),
        @JsonSubTypes.Type(value = ConditionalStep.class, name = "conditional"),
        @JsonSubTypes.Type(value = ForeachStep.class, name = "foreach")
})
@Slf4j
public abstract class Step implements CustomStepContainer {
    @NotNull
    @Builder.Default
    private final StepSpec<JsonSchema> spec = StepSpec.<JsonSchema>builder().
            patternType(PatternType.SIMPLE).build();

    @Singular
    @Valid
    private final List<Step> steps = new ArrayList<>();

    public void validate(final String fieldPrefix, final Errors errors, final StepExecutionResolverWrapper stepExecutionResolver) {
        for (int i = 0; i < this.steps.size(); i++) {
            this.steps.get(i).validate(fieldPrefix + "steps[" + i + "].", errors, stepExecutionResolver);
        }
    }

    public final StepResponse<Json> execute(final StepRequest<Json> request, final SessionExecutionContext sessionExecutionContext, final EvaluationContext evaluationContext, final StepExecutionResolverWrapper stepExecutionResolver, final ExpressionEvaluator expressionEvaluator) {
        final long start = System.currentTimeMillis();
        try {
            final StepResponse<Json> response = this.executeInternally(sessionExecutionContext, evaluationContext, stepExecutionResolver, expressionEvaluator);
            if (response.getDuration() == null) {
                response.setDuration(Duration.ofMillis(System.currentTimeMillis() - start));
            }
            return response;
        } catch (final Exception e) {
            return StepResponse.<Json>builder().
                    status(StepResponse.StepStatus.ERRONEOUS).
                    errorMessage(e.getMessage()).
                    duration(Duration.ofMillis(System.currentTimeMillis() - start)).
                    build();
        }
    }

    protected void executeNestedInstructions(final SessionExecutionContext sessionExecutionContext, final EvaluationContext evaluationContext, final StepExecutionResolverWrapper stepExecutionResolver, final NestedStepResponse.Context.ContextBuilder subResponses,
                                             final List<String> instructions) {
        for (final String instruction : instructions) {
            final StepExecution subStepExecution = this.getSafeStepExecution(stepExecutionResolver, subResponses, instruction);
            if (subStepExecution == null) {
                break;
            }
            final StepResponse<Json> subStepResponse = subStepExecution.execute(sessionExecutionContext, evaluationContext);
            subResponses.entry(NestedStepResponse.Entry.builder().
                    instruction(instruction).
                    response(subStepResponse).
                    build());
            if (subStepResponse.getStatus()!= StepResponse.StepStatus.PASSED) {
                log.debug("Cancel execution of {} due to a faulty instruction: {}", this, instruction);
                return;
            }
        }
    }

    protected StepExecution getSafeStepExecution(final StepExecutionResolverWrapper stepExecutionResolver, final NestedStepResponse.Context.ContextBuilder responsesBuilder, final String instruction) {
        final StepExecution execution = stepExecutionResolver.resolveStepExecution(instruction);
        if (execution != null) {
            return execution;
        }
        responsesBuilder.entry(NestedStepResponse.Entry.builder().
                instruction(instruction).
                response(
                        SessionService.buildResponseForMissingStep(instruction)
                ).build());
        return null;
    }

    protected abstract NestedStepResponse executeInternally(SessionExecutionContext sessionExecutionContext, EvaluationContext evaluationContext, final StepExecutionResolverWrapper stepExecutionResolver, ExpressionEvaluator expressionEvaluator);

    public interface StepExecutionResolverWrapper {
        StepExecution resolveStepExecution(String instruction);
    }

}
