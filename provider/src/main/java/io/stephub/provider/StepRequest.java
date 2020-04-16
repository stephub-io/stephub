package io.stephub.provider;

import lombok.*;

import java.util.List;

@AllArgsConstructor
@Data
@EqualsAndHashCode
@Builder
public class StepRequest {
    private String id;
    @Singular
    private List<Argument> arguments;
}
