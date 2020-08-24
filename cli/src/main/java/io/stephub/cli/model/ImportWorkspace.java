package io.stephub.cli.model;

import com.fasterxml.jackson.annotation.JsonView;
import io.stephub.server.api.model.Workspace;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class ImportWorkspace extends Workspace {
    public static class InfoView {

    }

    @JsonView(InfoView.class)
    private List<String> featureFiles = new ArrayList<>();
}
