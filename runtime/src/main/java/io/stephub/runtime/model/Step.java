package io.stephub.runtime.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.stephub.runtime.service.GherkinPatternMatcher;
import io.stephub.runtime.validation.ExpressionValidator;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.validation.Errors;

import javax.validation.constraints.NotEmpty;
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
        defaultImpl = Step.BasicStep.class,
        property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = Step.BasicStep.class, name = "basic"),
        @JsonSubTypes.Type(value = Step.ConditionalStep.class, name = "conditional"),
        @JsonSubTypes.Type(value = Step.ForeachStep.class, name = "foreach")
})
public abstract class Step {
    @NotEmpty
    private String pattern;

    public abstract void validate(String fieldPrefix, Errors errors, StepMatchResolver stepMatchResolver);

    public interface StepMatchResolver {
        GherkinPatternMatcher.StepMatch resolveStepMatch(String instruction);
    }

    @NoArgsConstructor
    @Getter
    @SuperBuilder
    @EqualsAndHashCode(callSuper = true)
    @ToString(callSuper = true)
    public static class BasicStep extends Step {
        private final List<String> instructions = new ArrayList<>();

        @Override
        public void validate(final String fieldPrefix, final Errors errors, final StepMatchResolver stepMatchResolver) {
            for (int i = 0; i < this.instructions.size(); i++) {
                if (stepMatchResolver.resolveStepMatch(this.instructions.get(i)) == null) {
                    errors.rejectValue(fieldPrefix + "instructions[" + i + "]", "msg.step.unknown", "Step definition not found");
                }
            }
        }
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @SuperBuilder
    @EqualsAndHashCode(callSuper = true)
    @ToString(callSuper = true)
    public static class ConditionalStep extends Step {
        @ExpressionValidator.Valid
        private String conditionExpression;
        private final List<String> truthyInstructions = new ArrayList<>();
        private final List<String> faultyInstructions = new ArrayList<>();

        @Override
        public void validate(final String fieldPrefix, final Errors errors, final StepMatchResolver stepMatchResolver) {
            for (int i = 0; i < this.truthyInstructions.size(); i++) {
                if (stepMatchResolver.resolveStepMatch(this.truthyInstructions.get(i)) == null) {
                    errors.rejectValue(fieldPrefix + "truthyInstructions[" + i + "]", "msg.step.unknown", "Step definition not found");
                }
            }
            for (int i = 0; i < this.faultyInstructions.size(); i++) {
                if (stepMatchResolver.resolveStepMatch(this.faultyInstructions.get(i)) == null) {
                    errors.rejectValue(fieldPrefix + "faultyInstructions[" + i + "]", "msg.step.unknown", "Step definition not found");
                }
            }
        }
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @SuperBuilder
    @EqualsAndHashCode(callSuper = true)
    @ToString(callSuper = true)
    public static class ForeachStep extends BasicStep {
        @ExpressionValidator.Valid
        private String itemsExpression;
    }

}
