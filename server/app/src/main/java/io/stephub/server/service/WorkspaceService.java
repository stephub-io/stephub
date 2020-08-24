package io.stephub.server.service;

import io.stephub.server.api.model.Workspace;
import io.stephub.server.api.rest.WorkspaceFinder;
import io.stephub.server.model.Context;

import java.util.List;

public interface WorkspaceService {
    Workspace createWorkspace(Context ctx, Workspace draft);

    Workspace getWorkspace(Context ctx, String wid);

    Workspace getWorkspaceInternal(String wid);

    List<Workspace> findWorkspaces(Context ctx, WorkspaceFinder finder);

    Workspace update(Context ctx, Workspace workspace);
}
