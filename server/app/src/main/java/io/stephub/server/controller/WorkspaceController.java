package io.stephub.server.controller;

import io.stephub.server.api.model.Workspace;
import io.stephub.server.api.rest.PageResult;
import io.stephub.server.api.rest.WorkspaceFinder;
import io.stephub.server.model.Context;
import io.stephub.server.service.WorkspaceService;
import io.stephub.server.service.WorkspaceValidator;
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

    @GetMapping("/workspaces")
    @ResponseBody
    public PageResult<Workspace> findWorkspaces(@ModelAttribute final Context ctx, final WorkspaceFinder finder) {
        final List<Workspace> workspaces = this.workspaceService.findWorkspaces(ctx, finder);
        return PageResult.<Workspace>builder().items(workspaces).total(workspaces.size()).build();
    }

    @GetMapping("/workspaces/{wid}")
    @ResponseBody
    public Workspace getWorkspace(@ModelAttribute final Context ctx, @PathVariable("wid") final String wid) {
        final Workspace workspace = this.workspaceService.getWorkspace(ctx, wid);
        this.workspaceValidator.validate(workspace);
        return workspace;
    }

    @PostMapping("/workspaces")
    @ResponseBody
    public Workspace createWorkspace(@ModelAttribute final Context ctx, @Valid @RequestBody final Workspace draft) {
        return this.workspaceService.createWorkspace(ctx, draft);
    }


}
