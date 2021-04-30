package io.stephub.server.api.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonValue;
import io.stephub.json.Json;
import io.stephub.json.schema.JsonSchema;
import io.stephub.provider.api.model.LogEntry;
import io.stephub.provider.api.model.StepResponse;
import io.stephub.provider.api.model.StepResponse.StepStatus;
import io.stephub.provider.api.model.spec.StepSpec;
import lombok.*;
import lombok.experimental.SuperBuilder;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.time.Duration;
import java.time.OffsetDateTime;
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
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = FunctionalExecution.class, name = Execution.FUNCTIONAL_STR),
        @JsonSubTypes.Type(value = LoadExecution.class, name = Execution.LOAD_STR)
})
public abstract class Execution {
    public final static String FUNCTIONAL_STR = "functional";
    public final static String LOAD_STR = "load";

    public enum ExecutionType {
        FUNCTIONAL(FunctionalExecution.class), LOAD(LoadExecution.class);

        private final Class<Execution> type;

        ExecutionType(final Class<? extends Execution> type) {
            this.type = (Class<Execution>) type;
        }

        public Class<Execution> getType() {
            return this.type;
        }

        @Override
        @JsonValue
        public String toString() {
            return this.name().toLowerCase();
        }
    }

    public enum ExecutionStatus {
        INITIATED, EXECUTING, COMPLETED, CANCELLED, STOPPING;

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
    private OffsetDateTime initiatedAt;
    @JsonFormat(shape = STRING)
    private OffsetDateTime startedAt;
    @JsonFormat(shape = STRING)
    private OffsetDateTime completedAt;
    private GherkinPreferences gherkinPreferences;

    @NotNull
    private RuntimeSession.SessionSettings sessionSettings;

    private boolean erroneous;
    private String errorMessage;


    @JsonTypeInfo(
            use = JsonTypeInfo.Id.NAME,
            property = "type")
    @JsonSubTypes({
            @JsonSubTypes.Type(value = StepItemResultNested.class, name = "nested"),
            @JsonSubTypes.Type(value = StepItemResultLeaf.class, name = "leaf")
    })
    public interface StepItemResult {
        Duration getDuration();

        StepStatus getStatus();

        List<ExecutionLogEntry> getLogs();

        String getErrorMessage();
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class StepItemResultNested implements StepItemResult {

        private List<StepItemResultGroup> groups = new ArrayList<>();

        private List<ExecutionLogEntry> logs;

        private String errorMessage;

        @Override
        public Duration getDuration() {
            final Duration duration = Duration.ofMillis(0);
            this.groups.forEach(
                    group -> group.steps.forEach(step -> duration.plus(step.getResult() != null ? step.getResult().getDuration() : Duration.ZERO)
                    ));
            return duration;
        }

        @Override
        public StepStatus getStatus() {
            StepStatus status = StepStatus.PASSED;
            ML:
            for (final StepItemResultGroup group : this.groups) {
                for (final FunctionalExecution.StepExecutionItem step : group.steps) {
                    if (step.getResult() != null) {
                        final StepStatus subStatus = step.getResult().getStatus();
                        if (subStatus != null && subStatus != StepStatus.PASSED) {
                            status = subStatus;
                            break ML;
                        }
                    }
                }
            }
            return status;
        }
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class StepItemResultGroup {
        private String name;

        private List<FunctionalExecution.StepExecutionItem> steps = new ArrayList<>();

    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class StepItemResultLeaf implements StepItemResult {
        @NotNull
        private StepStatus status;

        @NotNull
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        private Duration duration;

        private String errorMessage;

        @Valid
        private Json output;

        private List<ExecutionLogEntry> logs;

        public StepItemResultLeaf(final StepResponse<Json> from, final List<ExecutionLogEntry> logs) {
            this.status = from.getStatus();
            this.duration = from.getDuration();
            this.errorMessage = from.getErrorMessage();
            this.output = from.getOutput();
            this.logs = logs;
        }
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    @Builder
    public static class ExecutionLogEntry {
        private String message;
        private List<ExecutionLogAttachment> attachments;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    @Builder
    public static class ExecutionLogAttachment {
        private String id;
        private String fileName;
        private String contentType;
        private long size;

        public ExecutionLogAttachment(final String id, final LogEntry.LogAttachment attachment) {
            this.id = id;
            this.fileName = attachment.getFileName();
            this.contentType = attachment.getContentType();
            this.size = attachment.getContent().length;
        }
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class Stats {
        int passed;
        int failed;
        int erroneous;

        void add(final Stats stats) {
            this.passed += stats.passed;
            this.failed += stats.failed;
            this.erroneous += stats.erroneous;
        }
    }

    @NoArgsConstructor
    @Data
    @SuperBuilder
    @AllArgsConstructor
    @JsonTypeInfo(
            use = JsonTypeInfo.Id.NAME,
            property = "type")
    @JsonSubTypes({
            @JsonSubTypes.Type(value = FunctionalExecution.FunctionalExecutionStart.class, name = Execution.FUNCTIONAL_STR),
            @JsonSubTypes.Type(value = LoadExecution.LoadExecutionStart.class, name = Execution.LOAD_STR)
    })
    public abstract static class ExecutionStart<E extends Execution> {
        @Valid
        @Builder.Default
        private RuntimeSession.SessionSettings sessionSettings = new RuntimeSession.SessionSettings();

    }

    @JsonTypeInfo(
            use = JsonTypeInfo.Id.NAME,
            property = "type")
    @JsonSubTypes({
            @JsonSubTypes.Type(value = FunctionalExecution.StepExecutionItem.class, name = "step"),
            @JsonSubTypes.Type(value = FunctionalExecution.FeatureExecutionItem.class, name = "feature"),
            @JsonSubTypes.Type(value = FunctionalExecution.ScenarioExecutionItem.class, name = "scenario")
    })
    @SuperBuilder(toBuilder = true)
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
        private StepSpec<JsonSchema> stepSpec;
        private StepItemResult result;

        @Override
        public Stats getStats() {
            final Stats stats = new Stats();
            if (this.result != null && this.result.getStatus() != null) {
                switch (this.result.getStatus()) {
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
        private List<FunctionalExecution.ScenarioExecutionItem> scenarios;

        @Override
        public ExecutionStatus getStatus() {
            return getAggregatedStatus(this.scenarios.stream().map(s -> s.getStatus()).collect(Collectors.toList()));
        }

        @Override
        public Stats getStats() {
            final Stats stats = new Stats();
            for (final FunctionalExecution.ScenarioExecutionItem scenario : this.scenarios) {
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

    @SuperBuilder
    @Data
    @NoArgsConstructor
    public static abstract class StepSequenceExecutionItem extends ExecutionItem {
        @Builder.Default
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
    @AllArgsConstructor
    @SuperBuilder
    @Data
    @ToString(of = "name")
    public static class ScenarioExecutionItem extends StepSequenceExecutionItem {
        private String name;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @SuperBuilder
    @Data
    @ToString(of = "name")
    public static class FixtureExecutionItem extends StepSequenceExecutionItem implements Comparable<FixtureExecutionItem> {
        private Fixture.FixtureType type;
        private int priority;
        private String name;
        private boolean abortOnError;

        public FixtureExecutionItem(final Fixture from) {
            this.type = from.getType();
            this.name = from.getName();
            this.priority = from.getPriority();
            this.abortOnError = from.isAbortOnError();
            this.setSteps(from.getSteps().stream().map(
                    step -> StepExecutionItem.builder().step(step).build()
            ).collect(Collectors.toList()));
        }

        public FixtureExecutionItem(final FixtureExecutionItem from) {
            this.type = from.getType();
            this.name = from.getName();
            this.priority = from.getPriority();
            this.abortOnError = from.isAbortOnError();
            this.setSteps(from.getSteps().stream().map(
                    step -> StepExecutionItem.builder().step(step.getStep()).build()
            ).collect(Collectors.toList()));
        }



        @Override
        public int compareTo(final FixtureExecutionItem other) {
            return this.priority - other.priority;
        }
    }
}
