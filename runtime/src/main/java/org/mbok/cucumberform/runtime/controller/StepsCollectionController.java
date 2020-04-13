package org.mbok.cucumberform.runtime.controller;

import org.mbok.cucumberform.provider.spec.StepSpec;
import org.mbok.cucumberform.runtime.model.Context;
import org.mbok.cucumberform.runtime.service.ProvidersFacade;
import org.mbok.cucumberform.runtime.service.WorkspaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class StepsCollectionController {
    @Autowired
    private ProvidersFacade providersFacade;

    @Autowired
    private WorkspaceService workspaceService;

    @GetMapping("/workspaces/{wid}/stepsCollection")
    @ResponseBody
    public Map<String, List<StepSpec>> getStepSpecs(@ModelAttribute final Context ctx, @PathVariable("wid") final String wid) {
        return this.providersFacade.getStepsCollection(this.workspaceService.getWorkspace(ctx, wid));
    }
}
