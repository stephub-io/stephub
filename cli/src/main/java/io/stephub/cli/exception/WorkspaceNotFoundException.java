package io.stephub.cli.exception;

import io.stephub.server.api.model.Workspace;
import io.stephub.server.api.rest.PageResult;

public class WorkspaceNotFoundException extends RuntimeException {
    public WorkspaceNotFoundException(final String workspace, final PageResult<Workspace> result) {
        super(
                result.getItems().size() == 0 ?
                        ("Workspace with id or name '" + workspace + "' not found")
                        :
                        ("No unique workspace with id or name '" + workspace + "' found")
        );
    }
}