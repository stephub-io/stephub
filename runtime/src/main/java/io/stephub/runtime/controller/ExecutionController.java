package io.stephub.runtime.controller;

import io.stephub.runtime.model.Context;
import io.stephub.runtime.model.Execution;
import io.stephub.runtime.service.ExecutionPersistence;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

@RestController
public class ExecutionController {
    @Autowired
    private ExecutionPersistence executionPersistence;

    @GetMapping("/workspaces/{wid}/executions/{execId}")
    @ResponseBody
    public DeferredResult<ResponseEntity<Execution>> getExecution(@ModelAttribute final Context ctx,
                                                                  @PathVariable("wid") final String wid,
                                                                  @PathVariable("execId") final String execId,
                                                                  @RequestParam(name = "waitForCompletion", defaultValue = "false") final boolean waitForCompletion) {
        final DeferredResult<ResponseEntity<Execution>> deferredResult = new DeferredResult<>();
        deferredResult.onTimeout(() ->
                deferredResult.setErrorResult(
                        ResponseEntity.status(HttpStatus.ACCEPTED)
                                .body("Execution is still not completed, retry again")));
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
