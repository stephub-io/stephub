package io.stephub.server.api.model.gherkin;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.stephub.server.api.model.Identifiable;
import lombok.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class Feature implements Identifiable {
    private String name;

    @Valid
    private StepSequence background;

    @Singular
    @Valid
    private List<Scenario> scenarios = new ArrayList<>();

    @Singular
    private List<String> tags = new ArrayList<>();

    @Singular
    private List<String> comments = new ArrayList<>();

    @Override
    @JsonIgnore
    public String getId() {
        return this.name;
    }
}
