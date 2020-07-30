package io.stephub.server.api.model.gherkin;

import io.stephub.server.api.validation.IProviderValidator;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.List;

public interface Tagable {
    @Valid
    List<@NotEmpty String> getTags();
}
