package io.stephub.runtime.controller;

import io.stephub.runtime.model.Context;
import io.stephub.runtime.model.Execution;
import io.stephub.runtime.model.ExecutionInstruction;
import io.stephub.runtime.model.RuntimeSession;
import io.stephub.runtime.service.ProvidersFacade;
import io.stephub.runtime.service.SessionService;
import io.stephub.runtime.service.WorkspaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
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
    public RuntimeSession startSession(@ModelAttribute final Context ctx, @PathVariable("wid") final String wid, @RequestBody final RuntimeSession.SessionStart sessionStart) {
        return this.sessionService.startSession(ctx, wid, sessionStart);
    }

    @GetMapping("/workspaces/{wid}/sessions/{sid}")
    @ResponseBody
    public RuntimeSession getSession(@ModelAttribute final Context ctx, @PathVariable("wid") final String wid,
                                     @PathVariable("sid") final String sid) {
        return this.sessionService.getSession(ctx, wid, sid);
    }

    @PostMapping("/workspaces/{wid}/sessions/{sid}/execute")
    public void executeWithinSession(@ModelAttribute final Context ctx, @PathVariable("wid") final String wid,
                                     @PathVariable("sid") final String sid,
                                     @RequestBody @Valid final ExecutionInstruction instruction,
                                     final HttpServletResponse response) throws IOException {
        final Execution execution = this.sessionService.startExecution(ctx, wid, sid, instruction);
        response.sendRedirect("../../executions/" + execution.getId() + "?waitForCompletion=true");
    }

    @DeleteMapping("/workspaces/{wid}/sessions/{sid}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void stopSession(@ModelAttribute final Context ctx, @PathVariable("wid") final String wid,
                            @PathVariable("sid") final String sid) {
        this.sessionService.stopSession(ctx, wid, sid);
    }
}
