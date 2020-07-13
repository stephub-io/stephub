package io.stephub.cli.command;

import io.stephub.cli.client.ExecutionClient;
import io.stephub.cli.client.WorkspaceClient;
import io.stephub.server.api.model.Execution;
import io.stephub.server.api.model.ExecutionInstruction;
import io.stephub.server.api.model.Workspace;
import lombok.extern.slf4j.Slf4j;
import me.tongfei.progressbar.DelegatingProgressBarConsumer;
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import picocli.CommandLine;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static io.stephub.server.api.model.Execution.ExecutionStatus.COMPLETED;
import static io.stephub.server.api.model.Execution.ExecutionStatus.INITIATED;

@Component
@CommandLine.Command(name = "execute", mixinStandardHelpOptions = true,
        exitCodeOnExecutionException = 34,
        subcommands = {
                ExecuteCommand.ExecuteStepCommand.class
        })
@Slf4j
public class ExecuteCommand {

    public static class ExecuteBaseCommand extends BaseCommand {
        @Autowired
        protected WorkspaceClient workspaceClient;

        @Autowired
        protected ExecutionClient executionClient;

        @CommandLine.Option(names = {"-w", "--workspace"}, description = "Workspace name or id", required = true)
        protected String workspace;
    }

    @Component
    @CommandLine.Command(name = "step", mixinStandardHelpOptions = true,
            exitCodeOnExecutionException = 34)
    @Slf4j
    public static class ExecuteStepCommand extends ExecuteBaseCommand implements Runnable {
        @CommandLine.Parameters(arity = "1..*", description = "Step to execute")
        private List<String> steps;


        @Override
        public void run() {
            final Workspace foundWorkspace = this.workspaceClient.findWorkspace(this.getServerContext(), this.workspace);
            log.info("Start execution of steps: {}", this.steps);
            final AtomicReference<ProgressBar> progressBar = new AtomicReference<>();
            final Execution execution = this.executionClient.executeAndWaitForCompletion(this.getServerContext(), foundWorkspace,
                    Execution.ExecutionStart.builder().instruction(
                            ExecutionInstruction.StepsExecutionInstruction.builder().
                                    steps(this.steps).build()
                    ).build(),
                    changedExecution -> {
                        if (progressBar.get() == null) {
                            progressBar.set(new ProgressBarBuilder()
                                    .setInitialMax(this.steps.size())
                                    .setTaskName("Executed steps")
                                    .setConsumer(new DelegatingProgressBarConsumer(log::info))
                                    .build());
                        }
                        progressBar.get().stepTo(changedExecution.getBacklog().stream().
                                filter(executionItem -> executionItem.getStatus() != INITIATED).
                                count());
                    }
            );
            if (progressBar.get() != null) {
                progressBar.get().stepTo(execution.getBacklog().stream().
                        filter(executionItem -> executionItem.getStatus() == COMPLETED).
                        count());
                progressBar.get().close();
            }
            log.info("Execution completed with following results: {}", execution);
        }
    }
}
