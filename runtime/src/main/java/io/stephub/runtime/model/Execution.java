package io.stephub.runtime.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonValue;
import io.stephub.json.Json;
import io.stephub.provider.api.model.StepResponse;
import lombok.*;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@EqualsAndHashCode
public class Execution {
    public static enum ExecutionStatus {
        EXECUTING, COMPLETED, INITIATED;

        @Override
        @JsonValue
        public String toString() {
            return this.name().toLowerCase();
        }
    }

    private String id;
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
    @Builder.Default
    private List<ExecutionResult> results = new ArrayList<>();

    @SuperBuilder
    @NoArgsConstructor
    @JsonTypeInfo(
            use = JsonTypeInfo.Id.NAME,
            include = JsonTypeInfo.As.PROPERTY,
            property = "type")
    @JsonSubTypes({
            @JsonSubTypes.Type(value = StepExecutionResult.class, name = "step")
    })
    public static class ExecutionResult {
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @SuperBuilder
    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class StepExecutionResult extends ExecutionResult {
        private String instruction;
        private StepResponse<Json> response;
    }
}
