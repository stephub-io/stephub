package io.stephub.runtime.model;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@EqualsAndHashCode
public class StepInstruction {
    private String instruction;
}
