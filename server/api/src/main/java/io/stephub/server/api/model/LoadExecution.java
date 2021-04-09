package io.stephub.server.api.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.stephub.json.schema.JsonSchema;
import io.stephub.provider.api.model.StepResponse;
import io.stephub.provider.api.model.spec.StepSpec;
import lombok.*;
import lombok.experimental.SuperBuilder;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING;

@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
public abstract class LoadExecution extends Execution {
    private LoadExecutionStart start;

    @Builder.Default
    private List<LoadSimulation> simulations = new ArrayList<>();


    @NoArgsConstructor
    @Data
    @EqualsAndHashCode
    @SuperBuilder
    @AllArgsConstructor
    public abstract static class LoadSimulation {
        private String name;
        private List<LoadScenario> scenarios;
        private UserLoadSpec userLoadSpec;

        public abstract int getCurrentTargetLoad();

        public abstract int getCurrentActualLoad();

        public abstract List<LoadRunner> getRunners();

    }

    public enum RunnerStatus {
        INITIATED, RUNNING, STOPPING, STOPPED;

        public boolean alive() {
            return this == INITIATED || this == RUNNING;
        }
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Data
    @ToString(of = "id")
    public static class LoadRunner {
        private String id;
        @JsonFormat(shape = STRING)
        private OffsetDateTime initiatedAt;
        @JsonFormat(shape = STRING)
        private OffsetDateTime startedAt;
        @JsonFormat(shape = STRING)
        private OffsetDateTime stoppedAt;
        private long iterationNumber;
        @Builder.Default
        private RunnerStatus status = RunnerStatus.INITIATED;
        @Builder.Default
        private List<FixtureExecutionItem> fixtures = new ArrayList<>();

        private String stopMessage;
    }

    @NoArgsConstructor
    @Data
    @EqualsAndHashCode
    @Builder
    @AllArgsConstructor
    @ToString(of = "id")
    public static class LoadScenario {
        @Builder.Default
        private String id = UUID.randomUUID().toString();
        private String name;
        private String featureName;
        private LoadStats stats;
        private List<LoadStep> steps;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    @Builder
    public static class LoadStep {
        private String step;
        private StepSpec<JsonSchema> spec;
        private LoadStats stats;
    }

    @NoArgsConstructor
    @Data
    @EqualsAndHashCode
    @Builder
    @AllArgsConstructor
    public static class LoadScenarioRun {
        private String scenarioId;
        @JsonFormat(shape = STRING)
        private OffsetDateTime startedAt;
        @JsonFormat(shape = STRING)
        private OffsetDateTime completedAt;
        private StepResponse.StepStatus status;

        private String errorMessage;
        private List<StepExecutionItem> steps;
    }

    @NoArgsConstructor
    @Data
    @EqualsAndHashCode(callSuper = true)
    @AllArgsConstructor
    public static class LoadStats extends Stats {
        int cancelled;
        private Duration min = null;
        private Duration max = null;
        private Duration avg = null;

        public static Duration addOpt(final Duration source, final Duration additive) {
            if (source == null) {
                return additive;
            }
            return source.plus(additive);
        }
    }


    @NoArgsConstructor
    @Data
    @EqualsAndHashCode
    @Builder
    @AllArgsConstructor
    public static class UserLoad {
        @JsonFormat(shape = STRING)
        private Date timestamp;

        private int amount;
    }


    @NoArgsConstructor
    @Data
    @EqualsAndHashCode
    @Builder
    @AllArgsConstructor
    public static class LoadExecutionStart extends ExecutionStart<LoadExecution> {
        @Valid
        @NotNull
        @Builder.Default
        private RuntimeSession.SessionSettings sessionSettings = new RuntimeSession.SessionSettings();

        @NotNull
        private Duration duration;

        @Valid
        @Size(min = 1)
        @Builder.Default
        private List<LoadSimulationSpec> simulations = new ArrayList<>();

    }

    @NoArgsConstructor
    @Data
    @EqualsAndHashCode
    @Builder
    @AllArgsConstructor
    public static class LoadSimulationSpec {
        @NotNull
        private String name;

        @NotNull
        @Valid
        private UserLoadSpec userLoad;

        @NotNull
        @Builder.Default
        @Valid
        private ExecutionInstruction.ScenariosExecutionInstruction selection = new ExecutionInstruction.ScenariosExecutionInstruction();
    }

    @JsonTypeInfo(
            use = JsonTypeInfo.Id.NAME,
            property = "type")
    @JsonSubTypes({
            @JsonSubTypes.Type(value = StaticUserLoadSpec.class, name = "static")
    })
    @SuperBuilder
    @Data
    @NoArgsConstructor
    public abstract static class UserLoadSpec {
        public abstract Duration nextChangeAfter(Duration simulationTime);

        public abstract int getAmountAt(Duration simulationTime);
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @SuperBuilder
    @Data
    public static class StaticUserLoadSpec extends UserLoadSpec {
        @Min(1)
        private int amount;


        @Override
        public Duration nextChangeAfter(final Duration simulationTime) {
            return null;
        }

        @Override
        public int getAmountAt(final Duration simulationTime) {
            return this.amount;
        }
    }
}
