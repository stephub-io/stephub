package io.stephub.cli.config.picocli;

import io.stephub.cli.command.LoggingMixin;
import io.stephub.cli.command.StepCtlMainCommand;
import io.stephub.json.Json;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.info.BuildProperties;
import org.springframework.stereotype.Component;
import picocli.CommandLine;
import picocli.CommandLine.IExecutionExceptionHandler;

@Component
@Slf4j
public class ApplicationRunner implements CommandLineRunner, ExitCodeGenerator {

    private final StepCtlMainCommand stepCtlMainCommand;

    private final CommandLine.IFactory factory; // auto-configured to inject PicocliSpringFactory

    private int exitCode;

    @Autowired(required = false)
    private BuildProperties buildProperties;

    @Autowired
    private JsonConverter jsonConverter;

    public ApplicationRunner(final StepCtlMainCommand stepCtlMainCommand, final CommandLine.IFactory factory) {
        this.stepCtlMainCommand = stepCtlMainCommand;
        this.factory = factory;
    }

    @Override
    public void run(final String... args) {
        final IExecutionExceptionHandler errorHandler = (ex, commandLine, parseResult) -> {
            if (log.isDebugEnabled()) {
                log.error("Command failed", ex);
            } else {
                log.error("Command failed: {}", ex.getMessage());
            }
            if (!(ex instanceof RuntimeException)) {
                commandLine.usage(commandLine.getErr());
            }
            return commandLine.getCommandSpec().exitCodeOnExecutionException();
        };
        this.exitCode = new CommandLine(this.stepCtlMainCommand, this.factory).
                setExecutionExceptionHandler(errorHandler).setExecutionStrategy(parseResult -> {
            LoggingMixin.configure(parseResult);
            log.info("CLI version: {}", this.buildProperties != null ? this.buildProperties.getVersion() : "unknown");
            return new CommandLine.RunLast().execute(parseResult);

        })
                .registerConverter(Json.class, this.jsonConverter)
                .execute(args);
    }

    @Override
    public int getExitCode() {
        return this.exitCode;
    }
}
