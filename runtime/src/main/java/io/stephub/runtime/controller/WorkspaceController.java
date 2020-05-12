package io.stephub.runtime.controller;

import io.stephub.runtime.model.Context;
import io.stephub.runtime.model.Workspace;
import io.stephub.runtime.service.WorkspaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
public class WorkspaceController {
    @Autowired
    private WorkspaceService workspaceService;

    @GetMapping("/workspaces/{wid}")
    @ResponseBody
    public Workspace startSessions(@ModelAttribute final Context ctx, @PathVariable("wid") final String wid) {
        return this.workspaceService.getWorkspace(ctx, wid, true);
    }

    @GetMapping("/workspaces")
    @ResponseBody
    public List<Workspace> getWorkspaces(@ModelAttribute final Context ctx) {
        return this.workspaceService.getWorkspaces(ctx);
    }

    @PostMapping("/workspaces")
    @ResponseBody
    public Workspace createWorkspace(@ModelAttribute final Context ctx, @Valid @RequestBody final Workspace draft) {
        return this.workspaceService.createWorkspace(ctx, draft);
    }


}
