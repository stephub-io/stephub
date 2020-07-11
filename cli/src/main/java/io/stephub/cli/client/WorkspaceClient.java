package io.stephub.cli.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.stephub.cli.exception.RemoteException;
import io.stephub.cli.exception.WorkspaceNotFoundException;
import io.stephub.server.api.model.Workspace;
import io.stephub.server.api.rest.PageResult;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
public class WorkspaceClient {
    @Autowired
    private OkHttpClient.Builder httpClientBuilder;

    @Autowired
    private ObjectMapper objectMapper;

    public Workspace findWorkspace(final ServerContext serverContext, final String workspace) {
        log.debug("Resolving workspace={} from {}", workspace, serverContext);
        final OkHttpClient client = this.httpClientBuilder.build();
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
                    throw new WorkspaceNotFoundException(workspace, result);
                }
                return result.getItems().get(0);
            } else {
                throw new RemoteException("Received unexpected HTTP status code (" + response.code() + ") for " + serverContext);
            }
        } catch (final IOException e) {
            throw new RemoteException("Failed to communicate to " + serverContext + ": " + e.getMessage(), e);
        }
    }

}
