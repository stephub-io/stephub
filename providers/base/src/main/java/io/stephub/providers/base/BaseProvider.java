package io.stephub.providers.base;

import lombok.extern.slf4j.Slf4j;
import io.stephub.providers.util.LocalProviderAdapter;
import io.stephub.providers.util.spring.SpringBeanProvider;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class BaseProvider extends SpringBeanProvider<LocalProviderAdapter.SessionState> {
    public static final String PROVIDER_NAME = "base";

    @Override
    protected SessionState startState(final String sessionId, final ProviderOptions options) {
        return SessionState.builder().build();
    }

    @Override
    protected void stopState(final SessionState state) {
    }

    @Override
    public String getName() {
        return PROVIDER_NAME;
    }
}