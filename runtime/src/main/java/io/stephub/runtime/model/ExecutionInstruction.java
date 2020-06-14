package io.stephub.runtime.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = ExecutionInstruction.StepExecutionInstruction.class, name = "step")
})
public class ExecutionInstruction {

    @Data
    @NoArgsConstructor
    public static class StepExecutionInstruction extends ExecutionInstruction {
        private String instruction;
    }
}
