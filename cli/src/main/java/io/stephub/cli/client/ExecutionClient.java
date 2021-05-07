package io.stephub.cli.client;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.stephub.cli.exception.RemoteException;
import io.stephub.server.api.model.Execution;
import io.stephub.server.api.model.FunctionalExecution;
import io.stephub.server.api.model.Workspace;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeTypeUtils;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static org.awaitility.Awaitility.await;

@Component
@Slf4j
public class ExecutionClient extends BaseClient {
    @Autowired
    private OkHttpClient.Builder httpClientBuilder;

    @Autowired
    private ObjectMapper objectMapper;

    public interface ExecutionListener {
        void onChange(FunctionalExecution execution);
    }

    public FunctionalExecution executeAndWaitForCompletion(final ServerContext serverContext, final Workspace workspace, final FunctionalExecution.FunctionalExecutionStart executionStart,
                                                           final ExecutionListener executionListener) {
        log.debug("Starting {} in {} at {}", executionStart, workspace, serverContext);
        final OkHttpClient client = this.httpClientBuilder.build();
        try {
            final Request request = new Request.Builder()
                    .post(RequestBody.create(this.objectMapper.writeValueAsString(executionStart), MediaType.get(MimeTypeUtils.APPLICATION_JSON_VALUE)))
                    .url(serverContext.getBaseApiUrl().
                            addPathSegment("workspaces").
                            addPathSegment(workspace.getId()).
                            addPathSegment("executions").
                            build())
                    .build();
            try (final Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    final FunctionalExecution execution = this.objectMapper.readValue(response.body().byteStream(), DefaultFunctionalExecution.class);
                    log.info("Execution started with id: {}", execution.getId());
                    final AtomicReference<FunctionalExecution> currentExecution = new AtomicReference<>(execution);
                    executionListener.onChange(execution);
                    await().pollInSameThread().atMost(1, TimeUnit.HOURS).pollInterval(5, TimeUnit.SECONDS).until(() -> {
                        if (currentExecution.get().getStatus() != FunctionalExecution.ExecutionStatus.COMPLETED) {
                            final FunctionalExecution newExecution = this.getExecution(serverContext, workspace.getId(), execution.getId());
                            if (this.getAllBacklogStatus(newExecution).equals(this.getAllBacklogStatus(currentExecution.get()))) {
                                executionListener.onChange(newExecution);
                            }
                            currentExecution.set(newExecution);
                        }
                        return currentExecution.get().getStatus() == FunctionalExecution.ExecutionStatus.COMPLETED;
                    });
                    return currentExecution.get();
                } else {
                    throw this.buildExceptionFromInvalidStatusCode(serverContext, response);
                }
            }
        } catch (final IOException e) {
            throw new RemoteException("Failed to communicate to " + serverContext + ": " + e.getMessage(), e);
        }
    }

    private List<FunctionalExecution.ExecutionStatus> getAllBacklogStatus(final FunctionalExecution execution) {
        return execution.getBacklog().stream().map(Execution.ExecutionItem::getStatus).collect(Collectors.toList());
    }

    private FunctionalExecution getExecution(final ServerContext serverContext, final String wid, final String execId) {
        final OkHttpClient client = this.httpClientBuilder.build();
        try {
            final Request request = new Request.Builder().
                    get().
                    url(serverContext.getBaseApiUrl().
                            addPathSegment("workspaces").
                            addPathSegment(wid).
                            addPathSegment("executions").
                            addPathSegment(execId).
                            build())
                    .build();
            final Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                return this.objectMapper.readValue(response.body().byteStream(), DefaultFunctionalExecution.class);
            } else {
                throw new RemoteException("Failed to resolve execution due to unexpected HTTP status code (" + response.code() + ") from " + serverContext);
            }
        } catch (final IOException e) {
            throw new RemoteException("Failed to resolve execution due to communication errors to " + serverContext + ": " + e.getMessage(), e);
        }
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
    private static class DefaultFunctionalExecution extends FunctionalExecution {
        @Override
        public int getRunnersCount() {
            return 0;
        }
    }
}
