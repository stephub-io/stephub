package io.stephub.server.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.stephub.json.schema.JsonSchema;
import io.stephub.provider.api.model.spec.StepSpec;
import lombok.*;
import lombok.experimental.SuperBuilder;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.stream.Collectors;

import static io.stephub.server.api.model.Execution.ExecutionStatus.*;

@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
public abstract class FunctionalExecution extends Execution {

    @NotNull
    private ExecutionInstruction instruction;

    @NotNull
    private RuntimeSession.SessionSettings sessionSettings;

    private GherkinPreferences gherkinPreferences;

    @NotNull
    @Builder.Default
    private List<ExecutionItem> backlog = new ArrayList<>();

    @JsonIgnore
    public abstract int getMaxParallelizationCount();

    public Stats getStats() {
        final Stats stats = new Stats();
        this.backlog.stream().forEach(i -> stats.add(i.getStats()));
        return stats;
    }


    @JsonTypeInfo(
            use = JsonTypeInfo.Id.NAME,
            property = "type")
    @JsonSubTypes({
            @JsonSubTypes.Type(value = StepExecutionItem.class, name = "step"),
            @JsonSubTypes.Type(value = FeatureExecutionItem.class, name = "feature"),
            @JsonSubTypes.Type(value = ScenarioExecutionItem.class, name = "scenario")
    })
    @SuperBuilder
    @Data
    @NoArgsConstructor
    public static abstract class ExecutionItem {
        private boolean erroneous;
        private String errorMessage;

        public abstract ExecutionStatus getStatus();

        public abstract Stats getStats();

    }

    @NoArgsConstructor
    @AllArgsConstructor
    @SuperBuilder
    @Data
    @EqualsAndHashCode(of = "id", callSuper = false)
    @ToString(of = "id")
    public static class StepExecutionItem extends ExecutionItem {
        @Builder.Default
        private String id = UUID.randomUUID().toString();
        @Builder.Default
        private ExecutionStatus status = INITIATED;
        private String step;
        private RoughStepResponse response;
        private StepSpec<JsonSchema> stepSpec;

        @Override
        public Stats getStats() {
            final Stats stats = new Stats();
            if (this.response != null) {
                switch (this.response.getStatus()) {
                    case PASSED:
                        stats.setPassed(1);
                        break;
                    case FAILED:
                        stats.setFailed(1);
                        break;
                    case ERRONEOUS:
                        stats.setErroneous(1);
                        break;
                }
            }
            return stats;
        }
    }


    @NoArgsConstructor
    @AllArgsConstructor
    @SuperBuilder
    @Data
    @ToString(of = "name")
    public static class FeatureExecutionItem extends ExecutionItem {
        private String name;
        private List<ScenarioExecutionItem> scenarios;

        @Override
        public ExecutionStatus getStatus() {
            return getAggregatedStatus(this.scenarios.stream().map(s -> s.getStatus()).collect(Collectors.toList()));
        }

        @Override
        public Stats getStats() {
            final Stats stats = new Stats();
            for (final ScenarioExecutionItem scenario : this.scenarios) {
                stats.add(scenario.getStats());
            }
            return stats;
        }

        static ExecutionStatus getAggregatedStatus(final List<ExecutionStatus> statusList) {
            if (statusList.isEmpty()) {
                return COMPLETED;
            }
            final Set<ExecutionStatus> agg = new HashSet<>(statusList);
            if (agg.contains(EXECUTING) || agg.contains(INITIATED) && agg.size() > 1) {
                return EXECUTING;
            } else if (agg.contains(COMPLETED)) {
                return COMPLETED;
            } else if (agg.contains(CANCELLED)) {
                return CANCELLED;
            }
            return INITIATED;
        }
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @SuperBuilder
    @Data
    @ToString(of = "name")
    public static class ScenarioExecutionItem extends ExecutionItem {
        private String name;
        @Singular
        private List<StepExecutionItem> steps = new ArrayList<>();

        @Override
        public ExecutionStatus getStatus() {
            final List<ExecutionStatus> statusList = new ArrayList<>();
            statusList.addAll(this.steps.stream().map(StepExecutionItem::getStatus).collect(Collectors.toList()));
            return FeatureExecutionItem.getAggregatedStatus(statusList);
        }

        @Override
        public Stats getStats() {
            final ExecutionStatus status = this.getStatus();
            if (status == COMPLETED || status == CANCELLED) {
                final Stats stepStats = new Stats();
                this.steps.stream().forEach(s -> stepStats.add(s.getStats()));
                if (stepStats.erroneous > 0) {
                    return new Stats(0, 0, 1);
                } else if (stepStats.failed > 0) {
                    return new Stats(0, 1, 0);
                } else if (stepStats.passed > 0) {
                    return new Stats(1, 0, 0);
                }
            }
            return new Stats();
        }
    }

    @NoArgsConstructor
    @Data
    @EqualsAndHashCode(callSuper = true)
    @SuperBuilder
    @AllArgsConstructor
    public static class FunctionalExecutionStart extends ExecutionStart<FunctionalExecution> {
        @Valid
        @NotNull
        private ExecutionInstruction instruction;

        @Min(1)
        @Builder.Default
        private int parallelSessionCount = 1;

        @Builder.Default
        private RuntimeSession.SessionSettings.ParallelizationMode parallelizationMode = RuntimeSession.SessionSettings.ParallelizationMode.SCENARIO;
    }
}
