package io.stephub.server.api.model.customsteps;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
import io.stephub.server.api.SessionExecutionContext;
import io.stephub.server.api.StepExecution;
import io.stephub.server.api.model.Identifiable;
import io.stephub.server.api.model.StepResponseContext;
import io.stephub.server.api.validation.ValidStepSpec;
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
        defaultImpl = BasicStepDefinition.class,
        property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = BasicStepDefinition.class, name = "basic"),
        @JsonSubTypes.Type(value = ConditionalStepDefinition.class, name = "conditional"),
        @JsonSubTypes.Type(value = ForeachStepDefinition.class, name = "foreach")
})
@Slf4j
public abstract class StepDefinition implements CustomStepContainer, Identifiable {
    @NotNull
    @Builder.Default
    @Valid
    @ValidStepSpec
    private final StepSpec<JsonSchema> spec = StepSpec.<JsonSchema>builder().
            patternType(PatternType.SIMPLE).build();

    @Valid
    @Builder.Default
    private final List<StepDefinition> stepDefinitions = new ArrayList<>();

    public void validate(final String fieldPrefix, final Errors errors, final StepExecutionResolverWrapper stepExecutionResolver) {
        for (int i = 0; i < this.stepDefinitions.size(); i++) {
            this.stepDefinitions.get(i).validate(fieldPrefix + "steps[" + i + "].", errors, stepExecutionResolver);
        }
    }

    public final void execute(final StepRequest<Json> request, final SessionExecutionContext sessionExecutionContext, final EvaluationContext evaluationContext, final StepExecutionResolverWrapper stepExecutionResolver, final ExpressionEvaluator expressionEvaluator,
                              final StepResponseContext responseContext) {
        final long start = System.currentTimeMillis();
        try {
            this.executeInternally(sessionExecutionContext, evaluationContext, stepExecutionResolver, expressionEvaluator,
                    responseContext);
            /**
             if (response.getDuration() == Duration.ZERO) {
             response.setDuration(Duration.ofMillis(System.currentTimeMillis() - start));
             }
             **/
        } catch (final Exception e) {
            responseContext.completeStep(
                    StepResponse.<Json>builder().
                            status(StepResponse.StepStatus.ERRONEOUS).
                            errorMessage(e.getMessage()).
                            duration(Duration.ofMillis(System.currentTimeMillis() - start)).
                            build());
        }
    }

    protected void executeNestedSteps(final SessionExecutionContext sessionExecutionContext, final EvaluationContext evaluationContext, final StepExecutionResolverWrapper stepExecutionResolver, final StepResponseContext.NestedResponseSequenceContext responseSequenceContext,
                                      final List<String> steps) {
        for (final String instruction : steps) {
            final StepExecution subStepExecution = this.getSafeStepExecution(stepExecutionResolver, responseSequenceContext, instruction);
            if (subStepExecution == null) {
                break;
            }
            final StepResponseContext stepResponseContext = responseSequenceContext.startStep(instruction);
            subStepExecution.execute(sessionExecutionContext, evaluationContext, stepResponseContext);
            if (!stepResponseContext.continuable()) {
                log.debug("Cancel execution of {} due to a faulty instruction: {}", this, instruction);
                return;
            }
        }
    }

    protected StepExecution getSafeStepExecution(final StepExecutionResolverWrapper stepExecutionResolver, final StepResponseContext.NestedResponseSequenceContext responseSequenceContext, final String instruction) {
        final StepExecution execution = stepExecutionResolver.resolveStepExecution(instruction);
        if (execution != null) {
            return execution;
        }
        responseSequenceContext.startStep(instruction).
                completeStep(
                        StepExecution.buildResponseForMissingStep(instruction)
                );
        return null;
    }

    protected abstract void executeInternally(SessionExecutionContext sessionExecutionContext, EvaluationContext evaluationContext, final StepExecutionResolverWrapper stepExecutionResolver, ExpressionEvaluator expressionEvaluator,
                                              StepResponseContext responseContext);

    public interface StepExecutionResolverWrapper {
        StepExecution resolveStepExecution(String instruction);
    }

    @Override
    @JsonIgnore
    public String getId() {
        return this.spec != null ? this.spec.getPatternType() + this.spec.getPattern() : null;
    }
}
