package io.stephub.server.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import lombok.experimental.SuperBuilder;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.*;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
@SuperBuilder
public abstract class FunctionalExecution extends Execution {

    @NotNull
    private ExecutionInstruction instruction;

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
