package io.stephub.cli.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.stephub.cli.exception.RemoteException;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

public class BaseClient {
    @Autowired
    private ObjectMapper objectMapper;

    protected RemoteException buildExceptionFromInvalidStatusCode(final ServerContext serverContext, final Response response) {
        final StringBuilder msg = new StringBuilder("Received unexpected HTTP status code (" + response.code() + ") from " + serverContext);
        if (response.body() != null) {
            msg.append("\n");
            if (response.body().contentType() != null && response.body().contentType().toString().contains("application/json")) {
                try {
                    msg.append(this.objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(this.objectMapper.readValue(response.body().bytes(), Object.class)));
                } catch (final IOException e) {

                }
            }
        }
        return new RemoteException(msg.toString());
    }
}
