package io.stephub.server.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.kagkarlsson.scheduler.Scheduler;
import com.github.kagkarlsson.scheduler.Serializer;
import com.github.kagkarlsson.scheduler.boot.autoconfigure.DbSchedulerAutoConfiguration;
import com.github.kagkarlsson.scheduler.boot.config.DbSchedulerCustomizer;
import com.github.kagkarlsson.scheduler.boot.config.DbSchedulerProperties;
import com.github.kagkarlsson.scheduler.boot.config.DbSchedulerStarter;
import com.github.kagkarlsson.scheduler.stats.StatsRegistry;
import com.github.kagkarlsson.scheduler.task.Task;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Configuration
public class DbSchedulerConfig {

    private final ObjectMapper objectMapper;

    public DbSchedulerConfig(final ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @AllArgsConstructor
    @Getter
    public static class RunnerTaskWrapper<T> {
        private final Task<T> task;
    }

    @AllArgsConstructor
    @Getter
    public static class MgmtTaskWrapper<T> {
        private final Task<T> task;
    }

    @Bean("mgmtSchedulerConfig")
    public DbSchedulerAutoConfiguration mgmtSchedulerConfig(final DataSource dataSource, final List<MgmtTaskWrapper<?>> mgmtTasks) {
        return new DbSchedulerAutoConfiguration(
                this.mgmtProperties(),
                dataSource,
                mgmtTasks.stream().map(MgmtTaskWrapper::getTask).collect(Collectors.toList())
        );
    }

    @Autowired
    @Bean("mgmtScheduler")
    public Scheduler mgmtScheduler(@Qualifier("mgmtSchedulerConfig") final DbSchedulerAutoConfiguration config) {
        return config.scheduler(this.customizer(), StatsRegistry.NOOP);
    }

    @Bean
    public DbSchedulerStarter mgmtSchedulerStarter(@Qualifier("mgmtSchedulerConfig") final DbSchedulerAutoConfiguration config) {
        return config.dbSchedulerStarter(this.mgmtScheduler(config));
    }

    @Bean
    @ConfigurationProperties(prefix = "mgmt.db-scheduler")
    public DbSchedulerProperties mgmtProperties() {
        return new DbSchedulerProperties();
    }

    @Bean("runnerSchedulerConfig")
    public DbSchedulerAutoConfiguration runnerSchedulerConfig(final DataSource dataSource, final List<RunnerTaskWrapper<?>> runnerTasks) {
        return new DbSchedulerAutoConfiguration(
                this.runnerProperties(),
                dataSource,
                runnerTasks.stream().map(RunnerTaskWrapper::getTask).collect(Collectors.toList())
        );
    }

    @Autowired
    @Bean("runnerScheduler")
    public Scheduler runnerScheduler(@Qualifier("runnerSchedulerConfig") final DbSchedulerAutoConfiguration config) {
        return config.scheduler(this.customizer(), StatsRegistry.NOOP);
    }

    @Bean
    public DbSchedulerStarter runnerSchedulerStarter(@Qualifier("runnerSchedulerConfig") final DbSchedulerAutoConfiguration config) {
        return config.dbSchedulerStarter(this.runnerScheduler(config));
    }

    @Bean
    @ConfigurationProperties(prefix = "runner.db-scheduler")
    public DbSchedulerProperties runnerProperties() {
        return new DbSchedulerProperties();
    }

    @Bean
    DbSchedulerCustomizer customizer() {
        return new DbSchedulerCustomizer() {
            @Override
            public Optional<Serializer> serializer() {
                return Optional.of(new Serializer() {
                    @Override
                    public byte[] serialize(final Object o) {
                        try {
                            return DbSchedulerConfig.this.objectMapper.writeValueAsBytes(o);
                        } catch (final JsonProcessingException e) {
                            throw new RuntimeException("Failed to serialize task data", e);
                        }
                    }

                    @Override
                    public <T> T deserialize(final Class<T> aClass, final byte[] bytes) {
                        try {
                            return DbSchedulerConfig.this.objectMapper.readValue(bytes, aClass);
                        } catch (final IOException e) {
                            throw new RuntimeException("Failed to deserialize task data", e);
                        }
                    }
                });
            }
        };
    }
}
