package io.stephub.provider.remote;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.stephub.json.Json;
import io.stephub.json.JsonObject;
import io.stephub.json.schema.JsonSchema;
import io.stephub.provider.api.Provider;
import io.stephub.provider.api.ProviderException;
import io.stephub.provider.api.model.ProviderInfo;
import io.stephub.provider.api.model.ProviderOptions;
import io.stephub.provider.api.model.StepRequest;
import io.stephub.provider.api.model.StepResponse;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;

import static io.stephub.json.jackson.ObjectMapperConfigurer.createObjectMapper;
import static org.apache.commons.lang3.Validate.notBlank;

@Slf4j
@Builder
public class RemoteProvider implements Provider<JsonObject, JsonSchema, Json> {
    public static final String HEADER_CONTENT_TYPE = "Content-Type";
    public static final String APPLICATION_JSON = "application/json; charset=UTF-8";
    @Builder.Default
    private final OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder();
    private final String baseUrl;
    @Builder.Default
    private final ObjectMapper objectMapper = createObjectMapper();

    private HttpUrl.Builder baseUrlBuilder() {
        return HttpUrl.parse(this.baseUrl).newBuilder();
    }

    public static class JsonProviderInfo extends ProviderInfo<JsonSchema> {

    }

    @Override
    public ProviderInfo<JsonSchema> getInfo() {
        final OkHttpClient client = this.httpClientBuilder.build();
        final Request request = new Request.Builder()
                .get()
                .url(this.baseUrl)
                .build();
        try (final Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                return this.objectMapper.readValue(response.body().byteStream(), JsonProviderInfo.class);
            } else {
                throw new ProviderException("Received wrong HTTP status code (" + response.code() + ") for accessing provider info from " + this);
            }
        } catch (final IOException e) {
            throw new ProviderException("Failed to access provider info from " + this, e);
        }
    }

    @Override
    public String createSession(final ProviderOptions options) {
        final OkHttpClient client = this.httpClientBuilder.build();
        try {
            final Request request = new Request.Builder()
                    .post(RequestBody.create(this.objectMapper.writeValueAsBytes(options)))
                    .header(HEADER_CONTENT_TYPE, APPLICATION_JSON)
                    .url(this.baseUrlBuilder().addPathSegment("sessions").build())
                    .build();
            final Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                final RemoteProviderSession session = this.objectMapper.readValue(response.body().byteStream(), RemoteProviderSession.class);
                log.debug("Started new session={} at {}", session, this);
                notBlank(session.id, "Provided empty session id from %s", this);
                return session.id;
            } else {
                throw new ProviderException("Received non 201 HTTP status code (" + response.code() + ") for starting new session at " + this);
            }
        } catch (final IOException e) {
            throw new ProviderException("Failed to start new session at " + this, e);
        }
    }

    public static class JsonStepResponse extends StepResponse<Json> {

    }

    @Override
    public StepResponse<Json> execute(final String sessionId, final StepRequest<Json> stepRequest) {
        final OkHttpClient client = this.httpClientBuilder.build();
        try {
            final Request request = new Request.Builder()
                    .post(RequestBody.create(this.objectMapper.writeValueAsBytes(stepRequest)))
                    .header(HEADER_CONTENT_TYPE, APPLICATION_JSON)
                    .url(this.baseUrlBuilder().addPathSegment("sessions").
                            addPathSegment(sessionId).addPathSegment("execute").build())
                    .build();
            final Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                final StepResponse<Json> stepResponse = this.objectMapper.readValue(response.body().byteStream(), JsonStepResponse.class);
                log.debug("Executed step={} within session={} with response={} at {}", stepRequest, sessionId, stepResponse, this);
                return stepResponse;
            } else {
                throw new ProviderException("Failed to execute step at " + this + " due to bad response (" + response.code() + ")" + (StringUtils.isNotBlank(response.message()) ? ": " + response.message() : ""));
            }
        } catch (final IOException e) {
            throw new ProviderException("Failed to execute step at " + this, e);
        }
    }

    @Override
    public void destroySession(final String sessionId) {
        final OkHttpClient client = this.httpClientBuilder.build();
        try {
            final Request request = new Request.Builder()
                    .delete()
                    .url(this.baseUrlBuilder().addPathSegment("sessions").
                            addPathSegment(sessionId).build())
                    .build();
            final Response response = client.newCall(request).execute();
            if (!response.isSuccessful()) {
                throw new ProviderException("Failed to delete session at " + this + " due to: " + response.message());
            }
            log.debug("Destroyed session {} at {}", sessionId, this);
        } catch (final IOException e) {
            throw new ProviderException("Failed to delete session at " + this, e);
        }
    }

    @Override
    public String toString() {
        return "RemoteProvider@" + this.baseUrl.toString() + "";
    }

    @Data
    public static class RemoteProviderSession {
        private String id;
    }
}
