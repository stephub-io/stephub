package io.stephub.providers.util.spring;

import io.stephub.json.JsonBoolean;
import io.stephub.json.JsonObject;
import io.stephub.provider.Provider;
import io.stephub.provider.StepRequest;
import io.stephub.provider.StepResponse;
import io.stephub.providers.util.LocalProviderAdapter.SessionState;
import io.stephub.providers.util.spring.StepMethodAnnotationProcessor.StepArgument;
import io.stephub.providers.util.spring.StepMethodAnnotationProcessor.StepMethod;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static java.time.Duration.ofMinutes;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {StepMethodAnnotationProcessor.class})
class StepMethodAnnotationProcessorTest {

    public static class TestProvider extends SpringBeanProvider<SessionState> {
        private final TestProvider mock = mock(TestProvider.class);

        private SessionState state;

        @StepMethod(pattern = "Bla bla")
        public StepResponse testStepNoArgs() {
            return this.mock.testStepNoArgs();
        }

        @StepMethod(pattern = "Bla bla multiple")
        public StepResponse testStepMultipleArgs(final SessionState someState,
                                                 @StepArgument(name = "enabled") final JsonBoolean arg1,
                                                 @StepArgument(name = "data") final JsonObject arg2) {
            return this.mock.testStepMultipleArgs(someState, arg1, arg2);
        }

        @Override
        protected SessionState startState(final String sessionId, final ProviderOptions options) {
            this.state = mock(SessionState.class);
            return this.state;
        }

        @Override
        protected void stopState(final SessionState state) {

        }

        @Override
        public String getName() {
            return "test";
        }
    }

    @SpyBean
    private TestProvider testProvider;

    @Test
    public void testStepNoArgs() {
        final String sid = this.testProvider.createSession(Provider.ProviderOptions.builder().sessionTimeout(ofMinutes(1)).build());
        this.testProvider.execute(sid, StepRequest.builder().id("testStepNoArgs").build());
        verify(this.testProvider.mock).testStepNoArgs();
    }

    @Test
    public void testStepMultipleArgs() {
        final String sid = this.testProvider.createSession(Provider.ProviderOptions.builder().sessionTimeout(ofMinutes(1)).build());
        this.testProvider.execute(sid, StepRequest.builder().
                id("testStepMultipleArgs").
                argument("data", new JsonObject()).
                argument("enabled", new JsonBoolean(true)).
                build());
        verify(this.testProvider.mock).testStepMultipleArgs(
                this.testProvider.state,
                new JsonBoolean(true),
                new JsonObject()
        );
    }

}