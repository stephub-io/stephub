package io.stephub.cli.config.picocli;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import picocli.CommandLine;

@Slf4j
public class PicocliSpringFactory implements CommandLine.IFactory {

    private final ApplicationContext applicationContext;

    public PicocliSpringFactory(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public <K> K create(Class<K> clazz) throws Exception {
        try {
            return getBeanOrCreate(clazz);
        } catch (Exception e) {
            log.trace("Unable to get bean of class {}, using default Picocli factory", clazz);
            return CommandLine.defaultFactory().create(clazz);
        }
    }

    private <K> K getBeanOrCreate(Class<K> clazz) {
        try {
            return applicationContext.getBean(clazz);
        } catch (Exception e) {
            return applicationContext.getAutowireCapableBeanFactory().createBean(clazz);
        }
    }
}
