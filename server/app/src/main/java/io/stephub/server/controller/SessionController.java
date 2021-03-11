package io.stephub.server.controller;

import io.stephub.server.api.model.RuntimeSession;
import io.stephub.server.model.Context;
import io.stephub.server.service.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class SessionController {

    @Autowired
    private SessionService sessionService;

    @GetMapping("/workspaces/{wid}/sessions")
    @ResponseBody
    public List<RuntimeSession> getSessions(@ModelAttribute final Context ctx, @PathVariable("wid") final String wid) {
        return this.sessionService.getSessions(ctx, wid);
    }

    @PostMapping("/workspaces/{wid}/sessions")
    @ResponseBody
    public RuntimeSession startSession(@ModelAttribute final Context ctx, @PathVariable("wid") final String wid, @RequestBody final RuntimeSession.SessionSettings sessionSettings) throws BindException {
        return this.sessionService.startSession(ctx, wid, sessionSettings);
    }

    @GetMapping("/workspaces/{wid}/sessions/{sid}")
    @ResponseBody
    public RuntimeSession getSession(@ModelAttribute final Context ctx, @PathVariable("wid") final String wid,
                                     @PathVariable("sid") final String sid) {
        return this.sessionService.getSession(ctx, wid, sid);
    }

    @DeleteMapping("/workspaces/{wid}/sessions/{sid}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void stopSession(@ModelAttribute final Context ctx, @PathVariable("wid") final String wid,
                            @PathVariable("sid") final String sid) {
        this.sessionService.stopSession(ctx, wid, sid);
    }
}
