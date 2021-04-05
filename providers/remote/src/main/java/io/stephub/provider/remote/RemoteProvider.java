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
import io.stephub.server.api.model.ProviderSpec;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static io.stephub.json.jackson.ObjectMapperConfigurer.createObjectMapper;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.Validate.notBlank;

@Slf4j
public class RemoteProvider implements Provider<JsonObject, JsonSchema, Json> {
    public static final String HEADER_CONTENT_TYPE = "Content-Type";
    public static final String APPLICATION_JSON = "application/json; charset=UTF-8";

    private OkHttpClient.Builder httpClientBuilder;

    private String baseUrl;
    private String alias;
    private ObjectMapper objectMapper = createObjectMapper();
    private Validator validator;

    @Component
    public static class DefaultProviderFactory implements RemoteProviderFactory {
        private Validator validator;

        private ObjectMapper objectMapper;

        @Value("${io.stephub.remoteProvider.readTimeoutSeconds:300}")
        private int readTimeoutSeconds;

        @Value("${io.stephub.remoteProvider.connectTimeoutSeconds:10}")
        private int connectTimeoutSeconds;

        @Value("${io.stephub.remoteProvider.writeTimeoutSeconds:10}")
        private int writeTimeoutSeconds;

        public DefaultProviderFactory(Validator validator, ObjectMapper objectMapper) {
            this.validator = validator;
            this.objectMapper = objectMapper;
        }

        @Override
        public RemoteProvider createProvider(final ProviderSpec remoteSpec) {
            final RemoteProvider provider = new RemoteProvider();
            provider.baseUrl = remoteSpec.getRemoteConfig().getUrl();
            provider.objectMapper = this.objectMapper;
            provider.validator = this.validator;
            provider.alias = remoteSpec.getName() +
                    (isNotBlank(remoteSpec.getVersion()) ? ":" + remoteSpec.getVersion() : "");
            provider.validator = this.validator;
            provider.httpClientBuilder = new OkHttpClient.Builder()
                    .connectTimeout(this.connectTimeoutSeconds, TimeUnit.SECONDS)
                    .readTimeout(this.readTimeoutSeconds, TimeUnit.SECONDS)
                    .writeTimeout(this.writeTimeoutSeconds, TimeUnit.SECONDS);
            return provider;
        }
    }

    private HttpUrl.Builder baseUrlBuilder() {
        return HttpUrl.parse(this.baseUrl).newBuilder();
    }

    public static class JsonProviderInfo extends ProviderInfo<JsonSchema> {

    }

    private OkHttpClient buildClient() {
        if (StringUtils.isNotBlank(this.baseUrl)) {
            return this.httpClientBuilder.build();
        }
        throw new ProviderException("Undefined URL for provider " + this);
    }

    @Override
    public ProviderInfo<JsonSchema> getInfo() {
        final OkHttpClient client = this.buildClient();
        final Request request = new Request.Builder()
                .get()
                .url(this.baseUrl)
                .build();
        try (final Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                return this.objectMapper.readValue(response.body().byteStream(), JsonProviderInfo.class);
            } else {
                throw new ProviderException("Received unexpected HTTP status code (" + response.code() + ") for accessing provider " + this);
            }
        } catch (final IOException e) {
            throw new ProviderException("Failed to access provider " + this + ": " + e.getMessage(), e);
        }
    }

    @Override
    public String createSession(final ProviderOptions options) {
        final OkHttpClient client = this.buildClient();
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
            throw new ProviderException("Failed to start new session at " + this + ": " + e.getMessage(), e);
        }
    }

    public static class JsonStepResponse extends StepResponse<Json> {

    }

    @Override
    public StepResponse<Json> execute(final String sessionId, final StepRequest<Json> stepRequest) {
        final OkHttpClient client = this.buildClient();
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
                final Set<ConstraintViolation<StepResponse<Json>>> violations = this.validator.validate(stepResponse);
                if (violations.size() != 0) {
                    throw new ProviderException("Received invalid response from " + this + ": " + (violations.stream().map(v -> "'" + v.getPropertyPath().toString() + "' " + v.getMessage()).collect(Collectors.joining(", "))));
                }
                return stepResponse;
            } else {
                throw new ProviderException("Failed to execute step at " + this + " due to bad response (" + response.code() + ")" + (StringUtils.isNotBlank(response.message()) ? ": " + response.message() : ""));
            }
        } catch (final IOException e) {
            throw new ProviderException("Failed to execute step at " + this + ": " + e.getMessage(), e);
        }
    }

    @Override
    public void destroySession(final String sessionId) {
        final OkHttpClient client = this.buildClient();
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
            throw new ProviderException("Failed to delete session at " + this + ": " + e.getMessage(), e);
        }
    }

    void setBaseUrl(final String baseUrl) {
        this.baseUrl = baseUrl;
    }

    void setValidator(final Validator validator) {
        this.validator = validator;
    }

    @Override
    public String toString() {
        return "'" + this.alias + "@" + this.baseUrl + "'";
    }

    @Data
    public static class RemoteProviderSession {
        private String id;
    }
}
