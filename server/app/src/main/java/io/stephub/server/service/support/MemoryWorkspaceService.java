package io.stephub.server.service.support;

import io.stephub.server.api.model.Workspace;
import io.stephub.server.api.rest.WorkspaceFinder;
import io.stephub.server.model.Context;
import io.stephub.server.service.ResourceNotFoundException;
import io.stephub.server.service.WorkspaceService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class MemoryWorkspaceService implements WorkspaceService {
    private final List<Workspace> workspaces = new ArrayList<>();

    @Override
    public Workspace createWorkspace(final Context ctx, final Workspace draft) {
        draft.setId(UUID.randomUUID().toString());
        this.workspaces.add(draft);
        return draft;
    }

    @Override
    public Workspace getWorkspace(final Context ctx, final String wid) {
        final Workspace workspace = this.workspaces.stream().filter(w -> w.getId().equals(wid)).findFirst().
                orElseThrow(() -> new ResourceNotFoundException("Workspace not found with id " + wid));
        return workspace;
    }

    @Override
    public Workspace getWorkspaceInternal(final String wid) {
        return this.getWorkspace(null, wid);
    }

    @Override
    public List<Workspace> findWorkspaces(final Context ctx, final WorkspaceFinder finder) {
        return this.workspaces.stream().filter(w -> this.matches(w, finder)).collect(Collectors.toList());
    }

    private boolean matches(final Workspace workspace, final WorkspaceFinder finder) {
        if (StringUtils.isNotBlank(finder.getWorkspace())) {
            return finder.getWorkspace().equals(workspace.getId()) || finder.getWorkspace().equals(workspace.getName());
        } else {
            return true;
        }
    }

}
