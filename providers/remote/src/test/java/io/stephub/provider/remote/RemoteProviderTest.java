package io.stephub.provider.remote;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import io.stephub.json.*;
import io.stephub.json.schema.JsonSchema;
import io.stephub.provider.*;
import io.stephub.provider.spec.ArgumentSpec;
import io.stephub.provider.spec.DataTableSpec;
import io.stephub.provider.spec.PatternType;
import io.stephub.provider.spec.StepSpec;
import lombok.extern.slf4j.Slf4j;
import okhttp3.HttpUrl;
import org.apache.http.HttpHeaders;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.util.MimeTypeUtils;

import java.net.URISyntaxException;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static io.stephub.json.Json.JsonType.*;
import static io.stephub.json.jackson.ObjectMapperConfigurer.createObjectMapper;
import static io.stephub.provider.StepResponse.StepStatus.PASSED;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Slf4j
class RemoteProviderTest {
    private WireMockServer wireMockServer;
    private HttpUrl.Builder baseUrlBuilder;
    private ObjectMapper objectMapper;
    private RemoteProvider provider;

    @Test
    public void testProviderInfo() throws JsonProcessingException, URISyntaxException {
        // Given
        final ProviderInfo given = ProviderInfo.builder().
                name("myProvider").
                version("1.2.3").
                optionsSchema(JsonSchema.ofType(OBJECT)).
                steps(Collections.singletonList(
                        StepSpec.builder().
                                id("123").
                                pattern("abc").
                                argument(
                                        ArgumentSpec.builder().
                                                name("arg1").
                                                schema(JsonSchema.ofType(BOOLEAN)).
                                                build()
                                ).
                                argument(
                                        ArgumentSpec.builder().
                                                name("arg2").
                                                schema(JsonSchema.ofType(STRING)).
                                                build()
                                ).
                                patternType(PatternType.REGEX).
                                payload(StepSpec.PayloadType.DATA_TABLE).
                                dataTable(
                                        DataTableSpec.builder().
                                                column(
                                                        DataTableSpec.ColumnSpec.builder().
                                                                name("col1").schema(JsonSchema.ofType(BOOLEAN)).build()
                                                ).build()
                                ).
                                build()
                )).build();
        this.mockProviderInfo(given);

        // Call & expect
        assertThat(this.provider.getInfo(), equalTo(given));
    }

    @Test
    public void testStartSession() throws JsonProcessingException {
        // Given
        final String serializedSession = this.objectMapper.writeValueAsString(Collections.singletonMap("id", "def"));
        this.assertValidSchema(serializedSession, "/schema/session.json");
        final String url = this.baseUrlBuilder.addPathSegment("sessions").build().encodedPath();
        stubFor(post(urlEqualTo(url)).
                willReturn(
                        aResponse().
                                withStatus(201).
                                withHeader(HttpHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_JSON_VALUE).
                                withBody(serializedSession)
                ));

        // Call
        final String sessionId = this.provider.createSession(ProviderOptions.builder().
                sessionTimeout(Duration.ofMinutes(7)).
                options(
                        JsonObject.builder().field("baseUrl", new JsonString("http://stephub.io")).
                                build()
                ).build()
        );

        // Expect
        assertThat(sessionId, equalTo("def"));
        verify(postRequestedFor(urlEqualTo(url))
                .withHeader(RemoteProvider.HEADER_CONTENT_TYPE, WireMock.equalTo(RemoteProvider.APPLICATION_JSON)).
                        withRequestBody(matchingJsonPath("$.sessionTimeout", WireMock.equalTo("PT7M"))).
                        withRequestBody(matchingJsonPath("$.options.baseUrl", WireMock.equalTo("http://stephub.io")))
        );
    }

    @Test
    public void testDestroySession() {
        // Given
        final String sid = "x3x";
        final String url = this.baseUrlBuilder.addPathSegment("sessions").addPathSegment(sid).build().encodedPath();
        stubFor(delete(urlEqualTo(url)).
                willReturn(
                        aResponse().
                                withStatus(204)
                ));

        // Call
        this.provider.destroySession(sid);

        // Expect
        verify(deleteRequestedFor(urlEqualTo(url)));
    }

    @Test
    public void testDestroyUnknownSession() {
        // Given
        stubFor(delete(anyUrl()).
                willReturn(
                        aResponse().
                                withStatus(404).
                                withStatusMessage("Unknown session")
                ));

        // Call
        final ProviderException e = assertThrows(ProviderException.class, () -> {
            this.provider.destroySession("123");
        });
        log.debug("Expected exception", e);
        // Expect
        verify(deleteRequestedFor(urlEqualTo(this.baseUrlBuilder.addPathSegment("sessions").addPathSegment("123").build().encodedPath())));
    }


    @Test
    public void testExecStep() throws JsonProcessingException {
        // Given
        final String sid = "3x3";
        final StepResponse givenResponse = StepResponse.builder().
                status(PASSED).
                duration(Duration.ofSeconds(7)).
                outputs(
                        Collections.singletonMap("var1", new JsonNumber(3))
                ).
                build();
        final String serializedResponse = this.objectMapper.writeValueAsString(givenResponse);
        log.debug("Given response: {}", serializedResponse);
        this.assertValidSchema(serializedResponse, "/schema/step-response.json");
        final String url = this.baseUrlBuilder.
                addPathSegment("sessions").
                addPathSegment(sid).addPathSegment("execute").build().encodedPath();
        stubFor(post(urlEqualTo(url)).
                willReturn(
                        aResponse().
                                withStatus(200).
                                withHeader(HttpHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_JSON_VALUE).
                                withBody(serializedResponse)
                ));
        final StepRequest givenRequest = StepRequest.builder().id("456").
                argument("arg1", new JsonString("abc")).
                argument("arg2", JsonBoolean.TRUE).
                dataTable(Collections.singletonList(
                        Collections.singletonMap("col1", JsonArray.builder().
                                value(JsonBoolean.TRUE).value(JsonBoolean.FALSE).build())
                )).build();
        final String serializedRequest = this.objectMapper.writeValueAsString(givenRequest);
        log.debug("Given request: {}", serializedRequest);
        this.assertValidSchema(serializedRequest, "/schema/step-request.json");

        // Call
        final StepResponse actualResponse = this.provider.execute(sid, givenRequest);

        // Expect
        assertThat(actualResponse, equalTo(givenResponse));
        verify(postRequestedFor(urlEqualTo(url))
                .withHeader(RemoteProvider.HEADER_CONTENT_TYPE, WireMock.equalTo(RemoteProvider.APPLICATION_JSON)).
                        withRequestBody(equalToJson(serializedRequest))
        );
    }

    @BeforeEach
    public void before() {
        this.objectMapper = createObjectMapper();
        this.wireMockServer = new WireMockServer(options().dynamicPort());
        this.wireMockServer.start();
        configureFor("localhost", this.wireMockServer.port());
        this.baseUrlBuilder = HttpUrl.parse("http://localhost:" + this.wireMockServer.port() + "/myProvider").newBuilder();
        this.provider = RemoteProvider.builder().
                baseUrl(this.baseUrlBuilder.build()).build();
    }

    @AfterEach
    public void after() {
        this.wireMockServer.stop();
    }

    private void mockProviderInfo(final ProviderInfo info) throws JsonProcessingException {
        final Map<String, Object> rawJsonMap = new HashMap<>();
        rawJsonMap.put("name", info.getName());
        rawJsonMap.put("version", info.getVersion());
        rawJsonMap.put("optionsSchema", info.getOptionsSchema());
        rawJsonMap.put("steps", info.getSteps());
        final String serializedProviderInfo = this.objectMapper.writeValueAsString(rawJsonMap);
        log.debug("Mock provider info: {}", serializedProviderInfo);
        // Validate input against schema
        this.assertValidSchema(serializedProviderInfo, "/schema/provider-info.json");
        stubFor(get(urlEqualTo(this.baseUrlBuilder.build().encodedPath())).
                willReturn(
                        aResponse().
                                withHeader(HttpHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_JSON_VALUE).
                                withBody(serializedProviderInfo)
                ));
    }

    private void assertValidSchema(final String json, final String schemaPath) {
        Set<ValidationMessage> validationMessages = null;
        try {
            final JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V201909);
            final com.networknt.schema.JsonSchema schema = factory.getSchema(this.getClass().getResource(schemaPath).toURI());
            validationMessages = schema.validate(new ObjectMapper().readTree(json));
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
        assertThat(validationMessages, empty());
    }
}