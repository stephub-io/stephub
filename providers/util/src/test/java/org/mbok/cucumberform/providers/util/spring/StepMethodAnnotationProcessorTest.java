package org.mbok.cucumberform.providers.util.spring;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mbok.cucumberform.json.JsonBoolean;
import org.mbok.cucumberform.json.JsonObject;
import org.mbok.cucumberform.provider.Argument;
import org.mbok.cucumberform.provider.Provider;
import org.mbok.cucumberform.provider.StepRequest;
import org.mbok.cucumberform.provider.StepResponse;
import org.mbok.cucumberform.providers.util.LocalProviderAdapter.SessionState;
import org.mbok.cucumberform.providers.util.spring.StepMethodAnnotationProcessor.StepArgument;
import org.mbok.cucumberform.providers.util.spring.StepMethodAnnotationProcessor.StepMethod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static java.time.Duration.ofMinutes;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {StepMethodAnnotationProcessor.class, StepMethodAnnotationProcessorTest.SomeBean.class})
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

    @Component
    public static class SomeBean {
        private SomeBean mock = mock(SomeBean.class);

        @StepMethod(pattern = "Bla bla blub", provider = TestProvider.class)
        public StepResponse testStepExternalNoArgs() {
            return this.mock.testStepExternalNoArgs();
        }
    }

    @SpyBean
    private TestProvider testProvider;

    @Autowired
    private SomeBean externalBean;

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
                argument(Argument.builder().name("data").value(new JsonObject()).build()).
                argument(Argument.builder().name("enabled").value(new JsonBoolean(true)).build()).
                build());
        verify(this.testProvider.mock).testStepMultipleArgs(
                this.testProvider.state,
                new JsonBoolean(true),
                new JsonObject()
        );
    }

    @Test
    public void testExternalStepNoArgs() {
        final String sid = this.testProvider.createSession(Provider.ProviderOptions.builder().sessionTimeout(ofMinutes(1)).build());
        this.testProvider.execute(sid, StepRequest.builder().id("testStepExternalNoArgs").build());
        verify(this.externalBean.mock).testStepExternalNoArgs();
    }

}