package org.mbok.cucumberform.providers.wiremock;

import com.github.tomakehurst.wiremock.WireMockServer;
import lombok.Data;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import org.mbok.cucumberform.json.JsonString;
import org.mbok.cucumberform.providers.util.LocalProviderAdapter;
import org.mbok.cucumberform.provider.StepRequest;
import org.mbok.cucumberform.provider.StepResponse;
import org.mbok.cucumberform.providers.util.spring.SpringBeanProvider;
import org.mbok.cucumberform.providers.util.spring.StepMethodAnnotationProcessor;
import org.mbok.cucumberform.providers.util.spring.StepMethodAnnotationProcessor.StepArgument;
import org.mbok.cucumberform.providers.util.spring.StepMethodAnnotationProcessor.StepMethod;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

@Slf4j
public class WireMockProvider extends SpringBeanProvider<WirMockState> {
    @Override
    protected WirMockState startState(String sessionId, ProviderOptions options) {
        WireMockServer wireMockServer = new WireMockServer(wireMockConfig().dynamicPort());
        wireMockServer.start();
        log.debug("Started for sessionId={} local WirMock server on port={}", sessionId, wireMockServer.port());
        return WirMockState.builder().wireMockServer(wireMockServer).build();
    }

    @Override
    protected void stopState(String sessionId, WirMockState state) {
        state.getWireMockServer().stop();
        log.debug("Stopped for sessionId={} local WirMock server on port={}", sessionId, state.getWireMockServer().port());
    }

    @StepMethod(pattern = "I mock a GET request to {string:url}")
    public StepResponse mockSomething(@StepArgument(name = "url") JsonString url) {
        return null;
    }

}
@SuperBuilder
@Data
class WirMockState extends LocalProviderAdapter.SessionState {
    private WireMockServer wireMockServer;
}