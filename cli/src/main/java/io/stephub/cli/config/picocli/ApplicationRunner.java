package io.stephub.cli.config.picocli;

import io.stephub.cli.command.StepCtlMainCommand;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.stereotype.Component;
import picocli.CommandLine;

@Component
public class ApplicationRunner implements CommandLineRunner, ExitCodeGenerator {

    private final StepCtlMainCommand stepCtlMainCommand;

    private final CommandLine.IFactory factory; // auto-configured to inject PicocliSpringFactory

    private int exitCode;

    public ApplicationRunner(StepCtlMainCommand stepCtlMainCommand, CommandLine.IFactory factory) {
        this.stepCtlMainCommand = stepCtlMainCommand;
        this.factory = factory;
    }

    @Override
    public void run(String... args) throws Exception {
        exitCode = new CommandLine(stepCtlMainCommand, factory).execute(args);
    }

    @Override
    public int getExitCode() {
        return exitCode;
    }
}
