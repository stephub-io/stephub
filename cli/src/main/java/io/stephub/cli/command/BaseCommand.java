package io.stephub.cli.command;

import ch.qos.logback.classic.Level;
import io.stephub.cli.client.ServerContext;
import picocli.CommandLine;

import javax.annotation.PostConstruct;
import java.net.URL;

public class BaseCommand {
    @CommandLine.Mixin
    LoggingMixin loggingMixin;

    @CommandLine.Option(names = {"-s", "--server"}, description = "Runtime server URL", required = false,
            defaultValue = "http://localhost:7575", showDefaultValue = CommandLine.Help.Visibility.ALWAYS)
    protected URL serverUrl;

    protected ServerContext getServerContext() {
        return ServerContext.builder().baseUrl(this.serverUrl).build();
    }
}
