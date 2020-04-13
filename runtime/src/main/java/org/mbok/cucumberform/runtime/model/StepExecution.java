package org.mbok.cucumberform.runtime.model;

import lombok.*;
import org.mbok.cucumberform.provider.StepRequest;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@EqualsAndHashCode
public class StepExecution {
    private String instruction;
}
