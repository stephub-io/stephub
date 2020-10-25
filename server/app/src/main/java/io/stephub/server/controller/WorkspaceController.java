package io.stephub.server.controller;

import io.stephub.server.api.model.Workspace;
import io.stephub.server.api.model.gherkin.Feature;
import io.stephub.server.api.rest.PageResult;
import io.stephub.server.api.rest.WorkspaceFinder;
import io.stephub.server.model.Context;
import io.stephub.server.service.FeatureParser;
import io.stephub.server.service.WorkspaceService;
import io.stephub.server.service.WorkspaceValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;

@Slf4j
@RestController
public class WorkspaceController {
    @Autowired
    private WorkspaceService workspaceService;

    @Autowired
    private WorkspaceValidator workspaceValidator;

    @Autowired
    private FeatureParser featureParser;

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

    @PatchMapping("/workspaces/{wid}")
    @ResponseBody
    public Workspace patchWorkspace(@ModelAttribute final Context ctx, @PathVariable("wid") final String wid,
                                    @Valid @RequestBody final Workspace patch) {
        final Workspace workspace = this.workspaceService.patchWorkspace(ctx, wid, patch);
        this.workspaceValidator.validate(workspace);
        return workspace;
    }

    @PostMapping("/workspaces")
    @ResponseBody
    public Workspace createWorkspace(@ModelAttribute final Context ctx, @Valid @RequestBody final Workspace draft) {
        final Workspace workspace = this.workspaceService.createWorkspace(ctx, draft);
        this.workspaceValidator.validate(workspace);
        return workspace;
    }

    @PutMapping(value = "/workspaces/{wid}")
    @ResponseBody
    public Workspace updateWorkspace(@ModelAttribute final Context ctx, @PathVariable("wid") final String wid,
                                     @RequestBody @Valid final Workspace workspace) {
        log.debug("Updating workspace={}", wid);
        final Workspace existingWorkspace = this.workspaceService.getWorkspace(ctx, wid);
        workspace.setId(existingWorkspace.getId());
        final Workspace updated = this.workspaceService.update(ctx, workspace);
        this.workspaceValidator.validate(updated);
        return updated;
    }

    @PostMapping(value = "/workspaces/{wid}/features", consumes = TEXT_PLAIN_VALUE, produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    public Feature addFeature(@ModelAttribute final Context ctx, @PathVariable("wid") final String wid,
                              @RequestBody final String featureFile) {
        final Workspace workspace = this.workspaceService.getWorkspace(ctx, wid);
        final Feature parsedFeature = this.featureParser.parseFeature(workspace, featureFile);
        workspace.getFeatures().add(parsedFeature);
        this.workspaceService.update(ctx, workspace);
        return parsedFeature;
    }
}
