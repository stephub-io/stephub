package io.stephub.cli.command;

import io.stephub.cli.exception.CommandException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import picocli.CommandLine;

import java.io.File;

@Component
@CommandLine.Command(name = "import", mixinStandardHelpOptions = true,
        exitCodeOnExecutionException = 34)
@Slf4j
public class ImportCommand extends BaseCommand implements Runnable {

    @CommandLine.Parameters(paramLabel = "FILE",
            description = "one ore more workspace definition files to import",
            arity = "1..*")
    private File[] files;

    @Override
    public void run() {
    }
}
