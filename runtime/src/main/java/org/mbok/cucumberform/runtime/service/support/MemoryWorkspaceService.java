package org.mbok.cucumberform.runtime.service.support;

import org.mbok.cucumberform.runtime.model.Context;
import org.mbok.cucumberform.runtime.model.Workspace;
import org.mbok.cucumberform.runtime.service.ResourceNotFoundException;
import org.mbok.cucumberform.runtime.service.WorkspaceService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class MemoryWorkspaceService implements WorkspaceService {
    private final List<Workspace> workspaces = new ArrayList<>();

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
    public Workspace getWorkspace(final Context ctx, final String wid) {
        return this.workspaces.stream().filter(w -> w.getId().equals(wid)).findFirst().
                orElseThrow(() -> new ResourceNotFoundException("Workspace not found with id=" + wid));
    }
}
