package org.mbok.cucumberform.runtime.controller;

import org.mbok.cucumberform.runtime.model.Context;
import org.mbok.cucumberform.runtime.model.Workspace;
import org.mbok.cucumberform.runtime.service.WorkspaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
public class WorkspaceController {
    @Autowired
    private WorkspaceService workspaceService;

    @GetMapping("/workspaces")
    @ResponseBody
    public List<Workspace> getWorkspaces(@ModelAttribute final Context ctx) {
        return this.workspaceService.getWorkspaces(ctx);
    }

    @PostMapping("/workspaces")
    @ResponseBody
    public Workspace getWorkspaces(@ModelAttribute final Context ctx,@Valid @RequestBody final Workspace draft) {
        return this.workspaceService.createWorkspace(ctx, draft);
    }


}
