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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING;

@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
public class LoadExecution extends Execution {
    private LoadExecutionStart start;


    @NoArgsConstructor
    @Data
    @EqualsAndHashCode
    @Builder
    @AllArgsConstructor
    public static class LoadSimulation {
        private String name;
        private int currentUserLoad;
        private List<UserLoad> userLoadHistory;
        private List<LoadScenarioItem> scenarios;
    }

    @NoArgsConstructor
    @Data
    @EqualsAndHashCode
    @Builder
    @AllArgsConstructor
    public static class LoadScenarioItem {
        private String id;
        private String name;
        private LoadStats stats;

    }

    @NoArgsConstructor
    @Data
    @EqualsAndHashCode
    @Builder
    @AllArgsConstructor
    public static class LoadHistoryScenario {
        private String id;
        @JsonFormat(shape = STRING)
        private Date timestamp;

        private Duration duration;

        private StepResponse.StepStatus status;
        private String errorMessage;
        private List<LoadHistoryStep> steps;
    }

    @NoArgsConstructor
    @Data
    @EqualsAndHashCode
    @Builder
    @AllArgsConstructor
    public static class LoadHistoryStep {
        private String id;
        @JsonFormat(shape = STRING)
        private Date timestamp;
        private boolean cancelled;
        private RoughStepResponse response;
        private StepSpec<JsonSchema> stepSpec;
    }

    @NoArgsConstructor
    @Data
    @EqualsAndHashCode
    @Builder
    @AllArgsConstructor
    public static class LoadStepItem {
        private String id;
        private String step;
        private LoadStats stats;
    }

    @NoArgsConstructor
    @Data
    @EqualsAndHashCode(callSuper = true)
    @Builder
    @AllArgsConstructor
    public static class LoadStats extends Stats {
        private Duration min;
        private Duration max;
        private Duration avg;
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
    public static class LoadExecutionStart {
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
        private ExecutionInstruction.ScenarioFilter scenarioFilter = new ExecutionInstruction.AllScenarioFilter();
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
        public abstract Duration nextChangeAfter();

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
        public Duration nextChangeAfter() {
            return null;
        }

        @Override
        public int getAmountAt(final Duration simulationTime) {
            return this.amount;
        }
    }
}
