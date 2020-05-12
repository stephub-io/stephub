package io.stephub.runtime.controller;

import io.stephub.json.Json;
import io.stephub.provider.api.model.StepResponse;
import io.stephub.runtime.model.Context;
import io.stephub.runtime.model.RuntimeSession;
import io.stephub.runtime.model.StepExecution;
import io.stephub.runtime.service.ProvidersFacade;
import io.stephub.runtime.service.SessionService;
import io.stephub.runtime.service.WorkspaceService;
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
    public RuntimeSession startSession(@ModelAttribute final Context ctx, @PathVariable("wid") final String wid) {
        return this.sessionService.startSession(ctx, wid);
    }

    @GetMapping("/workspaces/{wid}/sessions/{sid}")
    @ResponseBody
    public RuntimeSession getSession(@ModelAttribute final Context ctx, @PathVariable("wid") final String wid,
                                     @PathVariable("sid") final String sid) {
        return this.sessionService.getSession(ctx, wid, sid);
    }

    @PostMapping("/workspaces/{wid}/sessions/{sid}/execute")
    @ResponseBody
    public StepResponse<Json> executeWithinSession(@ModelAttribute final Context ctx, @PathVariable("wid") final String wid,
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
