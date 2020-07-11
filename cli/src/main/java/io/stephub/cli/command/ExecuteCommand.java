package io.stephub.cli.command;

import io.stephub.cli.client.WorkspaceClient;
import io.stephub.server.api.model.Workspace;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import picocli.CommandLine;

@Component
@CommandLine.Command(name = "execute", mixinStandardHelpOptions = true,
        exitCodeOnExecutionException = 34)
@Slf4j
public class ExecuteCommand extends BaseCommand implements Runnable {
    @Autowired
    private WorkspaceClient workspaceClient;

    @CommandLine.Option(names = {"-w", "--workspace"}, description = "Workspace name or id", required = true)
    private String workspace;

    @Override
    public void run() {
        Workspace foundWorkspace = workspaceClient.findWorkspace(getServerContext(), this.workspace);
        log.info("Executed: " + this.workspace);
    }
}
