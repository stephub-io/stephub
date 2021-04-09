package io.stephub.server.api.model.gherkin;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.stephub.server.api.model.Identifiable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class Feature implements Identifiable, Annotatable {
    public static final String DEFAULT_TAG = "@default";
    private String name;

    @Valid
    private StepSequence background;

    @Valid
    @NotNull
    @Builder.Default
    private List<Scenario> scenarios = new ArrayList<>();

    @NotNull
    @Builder.Default
    private List<String> tags = new ArrayList<>(Collections.singletonList(DEFAULT_TAG));

    @NotNull
    @Builder.Default
    private List<String> comments = new ArrayList<>();

    @Override
    @JsonIgnore
    public String getId() {
        return this.name;
    }
}
