package io.stephub.server.api.model;

import com.fasterxml.jackson.annotation.*;
import io.stephub.json.Json;
import io.stephub.json.schema.JsonSchema;
import io.stephub.provider.api.model.StepResponse;
import io.stephub.provider.api.model.spec.StepSpec;
import lombok.*;
import lombok.experimental.SuperBuilder;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.stream.Collectors;

import static com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING;
import static io.stephub.server.api.model.Execution.ExecutionStatus.*;

@AllArgsConstructor
@NoArgsConstructor
@Data
@SuperBuilder
@EqualsAndHashCode(of = "id")
@ToString(of = "id")
public abstract class Execution implements Identifiable {
    public enum ExecutionStatus {
        INITIATED, EXECUTING, COMPLETED, CANCELLED;

        @Override
        @JsonValue
        public String toString() {
            return this.name().toLowerCase();
        }
    }

    private String id;
    @Builder.Default
    private ExecutionStatus status = ExecutionStatus.INITIATED;

    @JsonFormat(shape = STRING)
    private Date initiatedAt;
    @JsonFormat(shape = STRING)
    private Date startedAt;
    @JsonFormat(shape = STRING)
    private Date completedAt;

    @NotNull
    private ExecutionInstruction instruction;

    @NotNull
    private RuntimeSession.SessionSettings sessionSettings;

    private GherkinPreferences gherkinPreferences;

    @NotNull
    @Builder.Default
    private List<ExecutionItem> backlog = new ArrayList<>();

    private boolean erroneous;
    private String errorMessage;

    @JsonIgnore
    public abstract int getMaxParallelizationCount();

    public Stats getStats() {
        final Stats stats = new Stats();
        this.backlog.stream().forEach(i -> stats.add(i.getStats()));
        return stats;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class Stats {
        private int passed;
        private int failed;
        private int erroneous;

        void add(final Stats stats) {
            this.passed += stats.passed;
            this.failed += stats.failed;
            this.erroneous += stats.erroneous;
        }
    }

    @JsonTypeInfo(
            use = JsonTypeInfo.Id.NAME,
            include = JsonTypeInfo.As.PROPERTY,
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
        private ExecutionStatus status = ExecutionStatus.INITIATED;
        private String step;
        private StepResponse<Json> response;
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

        @Builder.Default
        private List<FixtureExecutionWrapper> beforeFixtures = new ArrayList<>();

        @Builder.Default
        private List<FixtureExecutionWrapper> afterFixtures = new ArrayList<>();

        @Override
        public ExecutionStatus getStatus() {
            final List<ExecutionStatus> statusList = new ArrayList<>();
            statusList.addAll(this.beforeFixtures.stream().map(FixtureExecutionWrapper::getStatus).collect(Collectors.toList()));
            statusList.addAll(this.steps.stream().map(StepExecutionItem::getStatus).collect(Collectors.toList()));
            statusList.addAll(this.afterFixtures.stream().map(FixtureExecutionWrapper::getStatus).collect(Collectors.toList()));
            return FeatureExecutionItem.getAggregatedStatus(statusList);
        }

        @Override
        public Stats getStats() {
            final ExecutionStatus status = this.getStatus();
            if (status == COMPLETED || status == CANCELLED) {
                final Stats stepStats = new Stats();
                this.beforeFixtures.stream().forEach(f -> stepStats.add(f.getStats()));
                this.steps.stream().forEach(s -> stepStats.add(s.getStats()));
                this.afterFixtures.stream().forEach(f -> stepStats.add(f.getStats()));
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
    @AllArgsConstructor
    @SuperBuilder
    @Data
    @ToString(of = "name")
    public static class FixtureExecutionWrapper {
        private String name;
        private List<StepExecutionItem> steps = new ArrayList<>();

        @JsonProperty
        public ExecutionStatus getStatus() {
            return FeatureExecutionItem.getAggregatedStatus(
                    this.steps.stream().map(s -> s.getStatus()).collect(Collectors.toList())
            );
        }

        @JsonIgnore
        public Stats getStats() {
            final Stats stats = new Stats();
            for (final StepExecutionItem step : this.steps) {
                stats.add(step.getStats());
            }
            return stats;
        }
    }

    @NoArgsConstructor
    @Data
    @EqualsAndHashCode
    @Builder
    @AllArgsConstructor
    public static class ExecutionStart {
        @Valid
        @NotNull
        private ExecutionInstruction instruction;
        @Valid
        @Builder.Default
        private RuntimeSession.SessionSettings sessionSettings = new RuntimeSession.SessionSettings();
        @Min(1)
        @Builder.Default
        private int parallelSessionCount = 1;
    }
}
