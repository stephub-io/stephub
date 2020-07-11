package io.stephub.cli.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.stephub.server.api.model.Workspace;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ExecutionClient {
    @Autowired
    private OkHttpClient.Builder httpClientBuilder;

    @Autowired
    private ObjectMapper objectMapper;

    public void execute(final ServerContext serverContext, final Workspace workspace) {

    }
}
