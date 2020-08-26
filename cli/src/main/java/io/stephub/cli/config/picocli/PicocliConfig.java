package io.stephub.cli.config.picocli;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import picocli.CommandLine;

@Configuration
@ConditionalOnClass(CommandLine.class)
public class PicocliConfig {
    @Primary
    @Bean
    @ConditionalOnMissingBean(CommandLine.IFactory.class)
    public CommandLine.IFactory picocliSpringFactory(ApplicationContext applicationContext) {
        return new PicocliSpringFactory(applicationContext);
    }

    @Bean
    @ConditionalOnMissingBean(PicocliSpringFactory.class)
    public PicocliSpringFactory picocliSpringFactoryImpl(ApplicationContext applicationContext) {
        return new PicocliSpringFactory(applicationContext);
    }
}
