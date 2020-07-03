package io.stephub.runtime.service;

import io.stephub.runtime.model.Context;
import io.stephub.runtime.model.Workspace;

import java.util.List;

public interface WorkspaceService {
    List<Workspace> getWorkspaces(Context ctx);

    Workspace createWorkspace(Context ctx, Workspace draft);

    Workspace getWorkspace(Context ctx, String wid);

    Workspace getWorkspaceInternal(String wid);
}
