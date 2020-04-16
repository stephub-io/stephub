package io.stephub.runtime.config;

import io.stephub.providers.base.BaseProvider;
import io.stephub.providers.util.LocalProviderAdapter;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackageClasses = {BaseProvider.class, LocalProviderAdapter.class})
public class ProvidersConfig {
}
