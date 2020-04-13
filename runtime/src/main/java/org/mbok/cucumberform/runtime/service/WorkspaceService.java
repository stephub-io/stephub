package org.mbok.cucumberform.runtime.service;

import org.mbok.cucumberform.runtime.model.Context;
import org.mbok.cucumberform.runtime.model.Workspace;

import java.util.List;

public interface WorkspaceService {
    List<Workspace> getWorkspaces(Context ctx);

    Workspace createWorkspace(Context ctx, Workspace draft);

    Workspace getWorkspace(Context ctx, String wid);
}
