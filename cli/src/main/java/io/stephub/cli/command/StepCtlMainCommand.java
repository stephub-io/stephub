package io.stephub.cli.command;

import org.springframework.stereotype.Component;
import picocli.CommandLine;
import picocli.CommandLine.Command;

@Component
@Command(name = "stepctl", mixinStandardHelpOptions = true,
        subcommands = {
                ImportCommand.class,
                ExecuteCommand.class
        })
public class StepCtlMainCommand {
        @CommandLine.Mixin
        LoggingMixin loggingMixin;
}
