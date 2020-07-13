package io.stephub.cli.command;

import ch.qos.logback.classic.Level;
import io.stephub.cli.client.ServerContext;
import picocli.CommandLine;

import java.net.URL;

public class BaseCommand {
    @CommandLine.Option(names = {"-s", "--server"}, description = "Runtime server URL", required = false,
            defaultValue = "http://localhost:7575", showDefaultValue = CommandLine.Help.Visibility.ALWAYS)
    protected URL serverUrl;

    @CommandLine.Option(names = "-v", scope = CommandLine.ScopeType.INHERIT, description = "Verbose output")
    // option is shared with subcommands
    public void setVerbose(final boolean[] verbose) {
        final ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
        root.setLevel(verbose.length > 0 ? Level.DEBUG : Level.INFO);
    }

    protected ServerContext getServerContext() {
        return ServerContext.builder().baseUrl(this.serverUrl).build();
    }
}
