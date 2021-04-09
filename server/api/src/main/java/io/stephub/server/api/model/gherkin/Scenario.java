package io.stephub.server.api.model.gherkin;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.stephub.server.api.model.Identifiable;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@SuperBuilder
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
public class Scenario extends StepSequence implements Annotatable, Identifiable {
    @NotNull
    private String name;

    @NotNull
    @Builder.Default
    private List<String> tags = new ArrayList<>();

    @NotNull
    @Builder.Default
    private List<String> comments = new ArrayList<>();

    @Override
    @JsonIgnore
    public String getId() {
        return this.getName();
    }
}
