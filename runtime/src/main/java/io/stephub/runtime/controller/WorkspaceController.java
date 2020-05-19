package io.stephub.runtime.controller;

import io.stephub.runtime.model.Context;
import io.stephub.runtime.model.Workspace;
import io.stephub.runtime.service.WorkspaceService;
import io.stephub.runtime.service.WorkspaceValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
public class WorkspaceController {
    @Autowired
    private WorkspaceService workspaceService;

    @Autowired
    private WorkspaceValidator workspaceValidator;

    @GetMapping("/workspaces/{wid}")
    @ResponseBody
    public Workspace getWorkspace(@ModelAttribute final Context ctx, @PathVariable("wid") final String wid) {
        final Workspace workspace = this.workspaceService.getWorkspace(ctx, wid);
        this.workspaceValidator.validate(workspace);
        return workspace;
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
