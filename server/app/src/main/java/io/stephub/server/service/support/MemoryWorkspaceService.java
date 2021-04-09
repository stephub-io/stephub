package io.stephub.server.service.support;

import io.stephub.providers.base.BaseProvider;
import io.stephub.server.api.model.Workspace;
import io.stephub.server.api.rest.WorkspaceFinder;
import io.stephub.server.model.Context;
import io.stephub.server.service.ResourceNotFoundException;
import io.stephub.server.service.WorkspaceService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class MemoryWorkspaceService implements WorkspaceService {
    private final Map<String, Workspace> workspaces = new HashMap<>();

    @Autowired
    private BaseProvider baseProvider;

    @Override
    public Workspace createWorkspace(final Context ctx, final Workspace draft) {
        draft.setId(UUID.randomUUID().toString());
        this.workspaces.put(draft.getId(), draft);
        return draft;
    }

    @Override
    public Workspace getWorkspace(final Context ctx, final String wid) {
        if (this.workspaces.containsKey(wid)) {
            return this.workspaces.get(wid);
        }
        throw new ResourceNotFoundException("Workspace not found with id " + wid);
    }

    @Override
    public Workspace getWorkspaceInternal(final String wid) {
        return this.getWorkspace(null, wid);
    }

    @Override
    public List<Workspace> findWorkspaces(final Context ctx, final WorkspaceFinder finder) {
        return this.workspaces.values().stream().filter(w -> this.matches(w, finder)).collect(Collectors.toList());
    }

    @Override
    public Workspace update(final Context ctx, final Workspace workspace) {
        if (this.getWorkspace(ctx, workspace.getId()) != null) {
            this.workspaces.put(workspace.getId(), workspace);
        }
        return workspace;
    }

    @Override
    public Workspace getTemplate(final Context ctx) {
        return new Workspace();
    }

    @Override
    public void deleteWorkspace(final Context ctx, final String wid) {
        this.workspaces.remove(wid, this.getWorkspace(ctx, wid));
    }

    private boolean matches(final Workspace workspace, final WorkspaceFinder finder) {
        if (StringUtils.isNotBlank(finder.getWorkspace())) {
            return finder.getWorkspace().equals(workspace.getId()) || finder.getWorkspace().equals(workspace.getName());
        } else {
            return true;
        }
    }

}
