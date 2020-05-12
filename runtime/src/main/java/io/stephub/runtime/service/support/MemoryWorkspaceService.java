package io.stephub.runtime.service.support;

import io.stephub.runtime.model.Context;
import io.stephub.runtime.model.Workspace;
import io.stephub.runtime.service.ResourceNotFoundException;
import io.stephub.runtime.service.WorkspaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class MemoryWorkspaceService implements WorkspaceService {
    private final List<Workspace> workspaces = new ArrayList<>();

    @Autowired
    private Validator validator;

    @Override
    public List<Workspace> getWorkspaces(final Context ctx) {
        return this.workspaces;
    }

    @Override
    public Workspace createWorkspace(final Context ctx, final Workspace draft) {
        draft.setId(UUID.randomUUID().toString());
        this.workspaces.add(draft);
        return draft;
    }

    @Override
    public Workspace getWorkspace(final Context ctx, final String wid, final boolean withValidation) {
        final Workspace workspace = this.workspaces.stream().filter(w -> w.getId().equals(wid)).findFirst().
                orElseThrow(() -> new ResourceNotFoundException("Workspace not found with id=" + wid));
        if (withValidation) {
            this.validate(workspace);
        }
        return workspace;
    }

    private void validate(final Workspace workspace) {
        final Set<ConstraintViolation<Workspace>> violations = this.validator.validate(workspace);
        if (!violations.isEmpty()) {
            workspace.setErrors(
                    violations.stream().map(cv ->
                            Workspace.Error.builder().
                                    path(cv.getPropertyPath().toString()).
                                    message(cv.getMessage()).build()).collect(Collectors.toList())
            );
        }
    }
}
