package io.stephub.runtime.service.support;

import io.stephub.runtime.model.Context;
import io.stephub.runtime.model.Workspace;
import io.stephub.runtime.service.ResourceNotFoundException;
import io.stephub.runtime.service.WorkspaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.SmartValidator;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class MemoryWorkspaceService implements WorkspaceService {
    private final List<Workspace> workspaces = new ArrayList<>();

    @Autowired
    private SmartValidator validator;

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
                orElseThrow(() -> new ResourceNotFoundException("Workspace not found with id " + wid));
        if (withValidation) {
            this.validate(workspace);
        }
        return workspace;
    }

    private void validate(final Workspace workspace) {
        final BeanPropertyBindingResult result = new BeanPropertyBindingResult(workspace, "workspace");
        this.validator.validate(workspace, result);
        if (result.hasErrors()) {
            workspace.setErrors(result.getAllErrors());
        } else {
            workspace.setErrors(null);
        }
    }
}
