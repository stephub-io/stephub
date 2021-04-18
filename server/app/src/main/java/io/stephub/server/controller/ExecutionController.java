package io.stephub.server.controller;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.stephub.provider.api.model.StepResponse;
import io.stephub.server.api.model.Execution;
import io.stephub.server.api.model.Execution.ExecutionType;
import io.stephub.server.api.model.FunctionalExecution;
import io.stephub.server.api.model.LoadExecution;
import io.stephub.server.api.rest.PageCriteria;
import io.stephub.server.api.rest.PageResult;
import io.stephub.server.model.Context;
import io.stephub.server.service.ExecutionPersistence;
import io.stephub.server.service.ExecutionService;
import io.stephub.server.service.exception.ExecutionException;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@Slf4j
public class ExecutionController {
    @Autowired
    private ExecutionPersistence executionPersistence;

    @Autowired
    private ExecutionService executionService;

    @Autowired
    private ListExecutionMapper executionMapper;

    @Component
    public static class ExecutionTypeConverter implements Converter<String, ExecutionType> {

        @Override
        public ExecutionType convert(final String value) {
            return ExecutionType.valueOf(value.toUpperCase());
        }
    }

    @PostMapping("/workspaces/{wid}/executions")
    public Execution startExecution(@ModelAttribute final Context ctx, @PathVariable("wid") final String wid,
                                    @RequestBody @Valid final Execution.ExecutionStart<? extends Execution> executionStart,
                                    final HttpServletResponse response) throws IOException, BindException {
        try {
            final Execution execution = this.executionService.startExecution(ctx, wid, executionStart);
            return execution;
        } catch (final ExecutionException e) {
            if (e.getCause() instanceof BindException) {
                throw (BindException) e.getCause();
            }
            throw e;
        }
    }

    @GetMapping("/workspaces/{wid}/executions")
    @ResponseBody
    public PageResult<Execution> getExecutions(@ModelAttribute final Context ctx,
                                               @PathVariable("wid") final String wid,
                                               @RequestParam(value = "type", required = false) final ExecutionType type) {
        final List<Execution> executions = this.executionPersistence.getExecutions(wid, type != null ? type.getType() : Execution.class);
        return PageResult.<Execution>builder().items(executions.stream()
                .map(execution -> execution instanceof FunctionalExecution ? this.executionMapper.mapWithoutBacklog((FunctionalExecution) execution) : execution).collect(Collectors.toList())).total(executions.size()).build();
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

    @GetMapping("/workspaces/{wid}/executions/{execId}/attachments/{attachmentId}")
    public ResponseEntity<InputStreamResource> getAttachment(@ModelAttribute final Context ctx,
                                                             @PathVariable("wid") final String wid,
                                                             @PathVariable("execId") final String execId,
                                                             @PathVariable("attachmentId") final String attachmentId) {
        final Pair<Execution.ExecutionLogAttachment, InputStream> attachment = this.executionPersistence.getLogAttachment(wid, execId, attachmentId);
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(attachment.getLeft().getContentType()));
        headers.setContentLength(attachment.getLeft().getSize());
        return new ResponseEntity<>(new InputStreamResource(attachment.getValue()), headers, HttpStatus.OK);
    }

    @GetMapping("/workspaces/{wid}/executions/{execId}/loadRuns")
    @ResponseBody
    public PageResult<LoadExecution.LoadScenarioRun> getLoadRuns(@ModelAttribute final Context ctx,
                                                                 @PathVariable("wid") final String wid,
                                                                 @PathVariable("execId") final String execId,
                                                                 @RequestParam(value = "simId", required = true) final String simId,
                                                                 @RequestParam(value = "status", defaultValue = "FAILED,ERRONEOUS") final List<StepResponse.StepStatus> status,
                                                                 final PageCriteria pageCriteria) {
        return this.executionPersistence.getLoadRuns(ctx, wid, execId, simId, status, pageCriteria);
    }

    @NoArgsConstructor
    @JsonTypeName(Execution.FUNCTIONAL_STR)
    public static class ListFunctionalExecution extends FunctionalExecution {

        @Override
        @JsonIgnore
        public int getMaxParallelizationCount() {
            return 0;
        }

        @JsonIgnore
        @Override
        public @NotNull List<ExecutionItem> getBacklog() {
            return super.getBacklog();
        }
    }

    @Mapper(componentModel = "spring")
    public interface ListExecutionMapper {
        ListFunctionalExecution mapWithoutBacklog(FunctionalExecution execution);
    }
}
