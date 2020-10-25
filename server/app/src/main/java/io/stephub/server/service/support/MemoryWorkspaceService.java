package io.stephub.server.service.support;

import io.stephub.server.api.model.Workspace;
import io.stephub.server.api.rest.WorkspaceFinder;
import io.stephub.server.model.Context;
import io.stephub.server.service.ResourceNotFoundException;
import io.stephub.server.service.WorkspaceService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class MemoryWorkspaceService implements WorkspaceService {
    private final Map<String, Workspace> workspaces = new HashMap<>();

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
    public Workspace patchWorkspace(final Context ctx, final String wid, final Workspace patch) {
        final Workspace workspace = this.getWorkspace(ctx, wid);
        if (workspace != null) {
            if (patch.getGherkinPreferences() != null) {
                workspace.setGherkinPreferences(patch.getGherkinPreferences());
            }
            if (patch.getName() != null) {
                workspace.setName(patch.getName());
            }
            if (patch.getVariables() != null) {
                workspace.setVariables(patch.getVariables());
            }
            if (patch.getBeforeFixtures() != null) {
                workspace.setBeforeFixtures(patch.getBeforeFixtures());
            }
            if (patch.getAfterFixtures() != null) {
                workspace.setAfterFixtures(patch.getAfterFixtures());
            }
            if (patch.getProviders() != null) {
                workspace.setProviders(patch.getProviders());
            }
            if (patch.getStepDefinitions() != null) {
                workspace.setStepDefinitions(patch.getStepDefinitions());
            }
            if (patch.getFeatures() != null) {
                workspace.setFeatures(patch.getFeatures());
            }
        }
        return workspace;
    }

    private boolean matches(final Workspace workspace, final WorkspaceFinder finder) {
        if (StringUtils.isNotBlank(finder.getWorkspace())) {
            return finder.getWorkspace().equals(workspace.getId()) || finder.getWorkspace().equals(workspace.getName());
        } else {
            return true;
        }
    }

}
