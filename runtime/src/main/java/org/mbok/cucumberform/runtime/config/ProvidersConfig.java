package org.mbok.cucumberform.runtime.config;

import org.mbok.cucumberform.providers.base.BaseProvider;
import org.mbok.cucumberform.providers.util.LocalProviderAdapter;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackageClasses = {BaseProvider.class, LocalProviderAdapter.class})
public class ProvidersConfig {
}
