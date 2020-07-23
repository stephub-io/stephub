package io.stephub.cli.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.stephub.cli.exception.RemoteException;
import io.stephub.server.api.model.Execution;
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
public class ExecutionClient {
    @Autowired
    private OkHttpClient.Builder httpClientBuilder;

    @Autowired
    private ObjectMapper objectMapper;

    public interface ExecutionListener {
        void onChange(Execution execution);
    }

    public Execution executeAndWaitForCompletion(final ServerContext serverContext, final Workspace workspace, final Execution.ExecutionStart executionStart,
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
                    final Execution execution = this.objectMapper.readValue(response.body().byteStream(), DefaultExecution.class);
                    log.info("Execution started with id: {}", execution.getId());
                    final AtomicReference<Execution> currentExecution = new AtomicReference<>(execution);
                    executionListener.onChange(execution);
                    await().pollInSameThread().atMost(1, TimeUnit.HOURS).pollInterval(5, TimeUnit.SECONDS).until(() -> {
                        if (currentExecution.get().getStatus() != Execution.ExecutionStatus.COMPLETED) {
                            final Execution newExecution = this.getExecution(serverContext, workspace.getId(), execution.getId());
                            if (this.getAllBacklogStatus(newExecution).equals(this.getAllBacklogStatus(currentExecution.get()))) {
                                executionListener.onChange(newExecution);
                            }
                            currentExecution.set(newExecution);
                        }
                        return currentExecution.get().getStatus() == Execution.ExecutionStatus.COMPLETED;
                    });
                    return currentExecution.get();
                } else {
                    throw buildUnexpectedStatusCodeException(serverContext, response);
                }
            }
        } catch (final IOException e) {
            throw new RemoteException("Failed to communicate to " + serverContext + ": " + e.getMessage(), e);
        }
    }

    private List<Execution.ExecutionStatus> getAllBacklogStatus(final Execution execution) {
        return execution.getBacklog().stream().map(Execution.ExecutionItem::getStatus).collect(Collectors.toList());
    }

    public static RemoteException buildUnexpectedStatusCodeException(final ServerContext serverContext, final Response response) throws IOException {
        String message = "Received unexpected HTTP status code (" + response.code() + ") for " + serverContext;
        if (response.body() != null) {
            message += "\n" + response.body().string();
        }
        return new RemoteException(message);
    }

    private Execution getExecution(final ServerContext serverContext, final String wid, final String execId) {
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
                return this.objectMapper.readValue(response.body().byteStream(), DefaultExecution.class);
            } else {
                throw new RemoteException("Failed to resolve execution due to unexpected HTTP status code (" + response.code() + ") for " + serverContext);
            }
        } catch (final IOException e) {
            throw new RemoteException("Failed to resolve execution due to communication errors to " + serverContext + ": " + e.getMessage(), e);
        }
    }

    private static class DefaultExecution extends Execution {

        @Override
        public int getMaxParallelizationCount() {
            return 0;
        }
    }
}
