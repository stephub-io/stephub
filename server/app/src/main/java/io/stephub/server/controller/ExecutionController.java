package io.stephub.server.controller;

import io.stephub.server.api.model.Execution;
import io.stephub.server.api.rest.PageResult;
import io.stephub.server.model.Context;
import io.stephub.server.service.ExecutionPersistence;
import io.stephub.server.service.ExecutionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.List;

@RestController
@Slf4j
public class ExecutionController {
    @Autowired
    private ExecutionPersistence executionPersistence;

    @Autowired
    private ExecutionService executionService;

    @PostMapping("/workspaces/{wid}/executions")
    public Execution startExecution(@ModelAttribute final Context ctx, @PathVariable("wid") final String wid,
                                    @RequestBody @Valid final Execution.ExecutionStart executionStart,
                                    final HttpServletResponse response) throws IOException {
        final Execution execution = this.executionService.startExecution(ctx, wid, executionStart);
        return execution;
    }

    @GetMapping("/workspaces/{wid}/executions")
    @ResponseBody
    public PageResult<Execution> getExecutions(@ModelAttribute final Context ctx,
                                               @PathVariable("wid") final String wid) {
        final List<Execution> executions = this.executionPersistence.getExecutions(wid);
        return PageResult.<Execution>builder().items(executions).total(executions.size()).build();
    }

    @GetMapping("/workspaces/{wid}/executions/{execId}")
    @ResponseBody
    public DeferredResult<ResponseEntity<Execution>> getExecution(@ModelAttribute final Context ctx,
                                                                  @PathVariable("wid") final String wid,
                                                                  @PathVariable("execId") final String execId,
                                                                  @RequestParam(name = "waitForCompletion", defaultValue = "false") final boolean waitForCompletion) {
        final DeferredResult<ResponseEntity<Execution>> deferredResult = new DeferredResult<>();
        deferredResult.onTimeout(() -> {
            if (waitForCompletion) {
                try {
                    deferredResult.setErrorResult(
                            ResponseEntity.ok(this.executionPersistence.getExecution(wid, execId, false).get())
                    );
                    return;
                } catch (final Exception e) {
                    log.warn("Failed to expose execution", e);
                }
            }
            deferredResult.setErrorResult(
                    ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body("Failed to load execution"));
        });
        this.executionPersistence.getExecution(wid, execId, waitForCompletion).whenCompleteAsync(
                (execution, throwable) -> {
                    if (throwable != null) {
                        deferredResult.setErrorResult(throwable);
                    } else {
                        deferredResult.setResult(ResponseEntity.ok(execution));
                    }
                });
        return deferredResult;
    }
}
