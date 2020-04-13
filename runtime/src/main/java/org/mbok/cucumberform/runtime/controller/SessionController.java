package org.mbok.cucumberform.runtime.controller;

import org.mbok.cucumberform.provider.StepResponse;
import org.mbok.cucumberform.runtime.model.Context;
import org.mbok.cucumberform.runtime.model.RuntimeSession;
import org.mbok.cucumberform.runtime.model.StepExecution;
import org.mbok.cucumberform.runtime.service.ProvidersFacade;
import org.mbok.cucumberform.runtime.service.SessionService;
import org.mbok.cucumberform.runtime.service.WorkspaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class SessionController {
    @Autowired
    private ProvidersFacade providersFacade;

    @Autowired
    private WorkspaceService workspaceService;

    @Autowired
    private SessionService sessionService;

    @GetMapping("/workspaces/{wid}/sessions")
    @ResponseBody
    public List<RuntimeSession> getSessions(@ModelAttribute final Context ctx, @PathVariable("wid") final String wid) {
        return this.sessionService.getSessions(ctx, wid);
    }

    @PostMapping("/workspaces/{wid}/sessions")
    @ResponseBody
    public RuntimeSession startSessions(@ModelAttribute final Context ctx, @PathVariable("wid") final String wid) {
        return this.sessionService.startSession(ctx, this.workspaceService.getWorkspace(ctx, wid));
    }

    @GetMapping("/workspaces/{wid}/sessions/{sid}")
    @ResponseBody
    public RuntimeSession getSession(@ModelAttribute final Context ctx, @PathVariable("wid") final String wid,
                                     @PathVariable("sid") final String sid) {
        return this.sessionService.getSession(ctx, wid, sid);
    }

    @PostMapping("/workspaces/{wid}/sessions/{sid}/execute")
    @ResponseBody
    public StepResponse executeWithinSession(@ModelAttribute final Context ctx, @PathVariable("wid") final String wid,
                                             @PathVariable("sid") final String sid,
                                             @RequestBody final StepExecution execution) {
        return this.sessionService.execute(ctx, wid, sid, execution);
    }

    @DeleteMapping("/workspaces/{wid}/sessions/{sid}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void stopSession(@ModelAttribute final Context ctx, @PathVariable("wid") final String wid,
                            @PathVariable("sid") final String sid) {
        this.sessionService.stopSession(ctx, wid, sid);
    }
}
