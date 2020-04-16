package io.stephub.runtime.model;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@EqualsAndHashCode
public class StepExecution {
    private String instruction;
}
