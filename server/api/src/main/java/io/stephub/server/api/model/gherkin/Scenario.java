package io.stephub.server.api.model.gherkin;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Singular;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@SuperBuilder
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
public class Scenario extends StepSequence implements Tagable {
    @Singular
    private List<String> tags = new ArrayList<>();
}
