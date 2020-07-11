package io.stephub.cli.command;

import org.springframework.stereotype.Component;
import picocli.CommandLine;

import java.util.List;
import java.util.concurrent.Callable;

@Component
@CommandLine.Command(name = "import", mixinStandardHelpOptions = true,
        exitCodeOnExecutionException = 34)
public class ImportCommand implements Callable<Integer> {
    @CommandLine.Option(names = "-y", description = "optional option")
    private String y;

    @CommandLine.Parameters(description = "positional params")
    private List<String> positionals;

    @Override
    public Integer call() {
        System.out.printf("mycommand sub was called with -y=%s and positionals: %s%n", this.y, this.positionals);
        return 33;
    }
}
