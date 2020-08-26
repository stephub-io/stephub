package io.stephub.cli.config.picocli;

import io.stephub.cli.command.LoggingMixin;
import io.stephub.cli.command.StepCtlMainCommand;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.stereotype.Component;
import picocli.CommandLine;
import picocli.CommandLine.IExecutionExceptionHandler;

@Component
@Slf4j
public class ApplicationRunner implements CommandLineRunner, ExitCodeGenerator {

    private final StepCtlMainCommand stepCtlMainCommand;

    private final CommandLine.IFactory factory; // auto-configured to inject PicocliSpringFactory

    private int exitCode;

    public ApplicationRunner(final StepCtlMainCommand stepCtlMainCommand, final CommandLine.IFactory factory) {
        this.stepCtlMainCommand = stepCtlMainCommand;
        this.factory = factory;
    }

    @Override
    public void run(final String... args)  {
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
                setExecutionExceptionHandler(errorHandler).setExecutionStrategy(LoggingMixin::executionStrategy).execute(args);
    }

    @Override
    public int getExitCode() {
        return this.exitCode;
    }
}
