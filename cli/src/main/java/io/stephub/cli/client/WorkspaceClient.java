package io.stephub.cli.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.stephub.cli.exception.RemoteException;
import io.stephub.cli.exception.WorkspaceNotFoundException;
import io.stephub.cli.model.ImportWorkspace;
import io.stephub.server.api.model.Workspace;
import io.stephub.server.api.rest.PageResult;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeTypeUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Component
@Slf4j
public class WorkspaceClient extends BaseClient {
    @Autowired
    private OkHttpClient.Builder httpClientBuilder;

    @Autowired
    private ObjectMapper objectMapper;

    public Workspace findWorkspace(final ServerContext serverContext, final String workspace, final boolean notFoundException) {
        log.info("Resolving workspace='{}' from {}", workspace, serverContext);
        final OkHttpClient client = this.httpClientBuilder.build();
        final Workspace resolvedWorkspace = this.getWorkspace(client, serverContext, workspace, notFoundException);
        if (resolvedWorkspace != null) {
            log.info("Resolved workspace='{}' successfully under id={}", workspace, resolvedWorkspace.getId());
        }
        return resolvedWorkspace;
    }

    private Workspace getWorkspace(final OkHttpClient client, final ServerContext serverContext, final String workspace, final boolean notFoundException) {
        final Request request = new Request.Builder()
                .get()
                .url(serverContext.getBaseApiUrl().
                        addPathSegment("workspaces").
                        addQueryParameter("workspace", workspace).
                        build())
                .build();
        try (final Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                final PageResult<Workspace> result = this.objectMapper.readValue(response.body().byteStream(), new TypeReference<>() {
                });
                log.debug("Resolved workspaces: {}", result);
                if (result.getItems().size() != 1) {
                    if (notFoundException || result.getItems().size() > 1) {
                        throw new WorkspaceNotFoundException(workspace, result);
                    } else {
                        return null;
                    }
                }
                return result.getItems().get(0);
            } else {
                throw this.buildExceptionFromInvalidStatusCode(serverContext, response);
            }
        } catch (final IOException e) {
            throw new RemoteException("Failed to communicate to " + serverContext + ": " + e.getMessage(), e);
        }
    }

    public Workspace replaceWorkspace(final ServerContext serverContext, final ImportWorkspace workspace) {
        log.info("Replacing workspace with id={} at {}", workspace.getId(), serverContext);
        final Workspace replacedWorkspace = this.sendWorkspace(serverContext, workspace,
                serverContext.getBaseApiUrl().
                        addPathSegment("workspaces").
                        addPathSegment(workspace.getId()).build(), "PUT");
        log.info("Replaced workspace with id={}", replacedWorkspace.getId());
        return replacedWorkspace;
    }

    public Workspace createWorkspace(final ServerContext serverContext, final ImportWorkspace workspace) {
        log.info("Creating workspace at {}", serverContext);
        final Workspace createdWorkspace = this.sendWorkspace(serverContext, workspace,
                serverContext.getBaseApiUrl().
                        addPathSegment("workspaces").build(), "POST");
        log.info("Created workspace with id={}", createdWorkspace.getId());
        return createdWorkspace;
    }

    private Workspace sendWorkspace(final ServerContext serverContext, final ImportWorkspace workspace,
                                    final HttpUrl url, final String method) {
        final OkHttpClient client = this.httpClientBuilder.build();
        try {
            final Request request = new Request.Builder()
                    .method(method, RequestBody.create(this.objectMapper.writeValueAsString(workspace), MediaType.get(MimeTypeUtils.APPLICATION_JSON_VALUE)))
                    .url(url)
                    .build();
            try (final Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    Workspace createdWorkspace = this.objectMapper.readValue(response.body().byteStream(), new TypeReference<>() {
                    });
                    createdWorkspace = this.uploadFeatureFiles(client, serverContext, createdWorkspace, workspace.getFeatureFiles());
                    return createdWorkspace;
                } else {
                    throw this.buildExceptionFromInvalidStatusCode(serverContext, response);
                }
            }
        } catch (final IOException e) {
            throw new RemoteException("Failed to communicate to " + serverContext + ": " + e.getMessage(), e);
        }
    }

    private Workspace uploadFeatureFiles(final OkHttpClient client, final ServerContext serverContext, final Workspace createdWorkspace, final List<String> featureFiles) {
        if (!featureFiles.isEmpty()) {
            for (final String featureFileStr : featureFiles) {
                final File featureFile = new File(featureFileStr);
                try {
                    log.info("Importing feature file='{}' to workspace={}", featureFile.getPath(), createdWorkspace.getId());
                    final Request request = new Request.Builder()
                            .method("POST",
                                    RequestBody.create(FileUtils.readFileToByteArray(featureFile), MediaType.get(MimeTypeUtils.TEXT_PLAIN_VALUE)))
                            .url(serverContext.getBaseApiUrl().
                                    addPathSegment("workspaces").
                                    addPathSegment(createdWorkspace.getId()).
                                    addPathSegment("features").build())
                            .build();
                    try (final Response response = client.newCall(request).execute()) {
                        if (response.isSuccessful()) {
                            log.info("Imported feature file='{}' to workspace={}", featureFile.getPath(), createdWorkspace.getId());
                        } else {
                            throw this.buildExceptionFromInvalidStatusCode(serverContext, response);
                        }
                    }
                } catch (final IOException e) {
                    throw new RemoteException("Failed to upload feature file '" + featureFile.getAbsolutePath() + "' to " + serverContext + ": " + e.getMessage(), e);
                }
            }
            return this.getWorkspace(client, serverContext, createdWorkspace.getId(), true);
        }
        return createdWorkspace;
    }
}
