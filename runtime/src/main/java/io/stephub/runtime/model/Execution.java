package io.stephub.runtime.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonValue;
import io.stephub.json.Json;
import io.stephub.provider.api.model.StepResponse;
import lombok.*;
import lombok.experimental.SuperBuilder;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.stream.Collectors;

import static com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING;
import static io.stephub.runtime.model.Execution.ExecutionStatus.*;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@EqualsAndHashCode(of = "id")
@ToString(of = "id")
public class Execution {
    public enum ExecutionStatus {
        INITIATED, EXECUTING, COMPLETED;

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

    @NotNull
    @Builder.Default
    private List<ExecutionItem> backlog = new ArrayList<>();

    private boolean erroneous;
    private String errorMessage;


    @JsonTypeInfo(
            use = JsonTypeInfo.Id.NAME,
            include = JsonTypeInfo.As.PROPERTY,
            property = "type")
    @JsonSubTypes({
            @JsonSubTypes.Type(value = StepExecutionItem.class, name = "step")
    })
    public interface ExecutionItem {
        ExecutionStatus getStatus();
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @SuperBuilder
    @Data
    @EqualsAndHashCode(of = "id")
    @ToString(of = "id")
    public static class StepExecutionItem implements ExecutionItem {
        @Builder.Default
        private String id = UUID.randomUUID().toString();
        @Builder.Default
        private ExecutionStatus status = ExecutionStatus.INITIATED;
        private String instruction;
        private StepResponse<Json> response;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @SuperBuilder
    @Data
    @ToString(of = "name")
    public static class FeatureExecutionItem implements ExecutionItem {
        private String name;
        private List<ScenarioExecutionItem> scenarios;

        @Override
        public ExecutionStatus getStatus() {
            return getAggregatedStatus(this.scenarios.stream().map(s -> s.getStatus()).collect(Collectors.toList()));
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
            }
            return INITIATED;
        }
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @SuperBuilder
    @Data
    @ToString(of = "name")
    public static class ScenarioExecutionItem implements ExecutionItem {
        private String name;
        @Singular
        private List<StepExecutionItem> steps = new ArrayList<>();

        private List<FixtureExecutionItem> beforeFixtures = new ArrayList<>();

        private List<FixtureExecutionItem> afterFixtures = new ArrayList<>();

        @Override
        public ExecutionStatus getStatus() {
            final List<ExecutionStatus> statusList = new ArrayList<>();
            statusList.addAll(this.beforeFixtures.stream().map(f -> f.getStatus()).collect(Collectors.toList()));
            statusList.addAll(this.steps.stream().map(s -> this.getStatus()).collect(Collectors.toList()));
            statusList.addAll(this.afterFixtures.stream().map(f -> f.getStatus()).collect(Collectors.toList()));
            return FeatureExecutionItem.getAggregatedStatus(statusList);
        }
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @SuperBuilder
    @Data
    @ToString(of = "name")
    public static class FixtureExecutionItem implements ExecutionItem {
        private String name;
        private List<StepExecutionItem> steps = new ArrayList<>();

        @Override
        public ExecutionStatus getStatus() {
            return FeatureExecutionItem.getAggregatedStatus(
                    this.steps.stream().map(s -> s.getStatus()).collect(Collectors.toList())
            );
        }
    }

    @NoArgsConstructor
    @Data
    @EqualsAndHashCode
    public static class ExecutionStart {
        @Valid
        private ExecutionInstruction instruction;
        @Valid
        private RuntimeSession.SessionSettings sessionSettings;
        @Min(1)
        private final int parallelSessionCount = 1;
    }
}
