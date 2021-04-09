package io.stephub.server.api.model.gherkin;

import java.util.List;

public interface Annotatable extends Tagable {
    List<String> getComments();
}
