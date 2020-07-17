package io.stephub.cli.command;

import com.jakewharton.fliptables.FlipTableConverters;
import io.stephub.cli.client.ExecutionClient;
import io.stephub.cli.client.WorkspaceClient;
import io.stephub.provider.api.model.StepResponse;
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

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;
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

        protected static String formatDuration(final Duration duration) {
            final long seconds = duration.getSeconds();
            final long absSeconds = Math.abs(seconds);
            final String positive = String.format(
                    "%d:%02d:%02d.%03d",
                    absSeconds / 3600,
                    (absSeconds % 3600) / 60,
                    absSeconds % 60,
                    TimeUnit.MILLISECONDS.convert(duration.getNano(), TimeUnit.NANOSECONDS));
            return seconds < 0 ? "-" + positive : positive;
        }

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
            if (execution.isErroneous()) {
                log.error("Execution failed: {}\n{}", execution.getErrorMessage(), this.formatResult(execution));
            } else {
                log.info("Execution completed successfully\n{}", this.formatResult(execution));
            }
        }

        private String formatResult(final Execution execution) {
            final String[] headers = {"Step", "Execution", "Status", "Duration"};
            final int c = execution.getBacklog().size();
            final String[][] data = new String[c][];
            for (int i = 0; i < c; i++) {
                final Execution.StepExecutionItem sei = (Execution.StepExecutionItem) execution.getBacklog().get(i);
                final String eStatus;
                if (sei.isErroneous()) {
                    eStatus = "ERRONEOUS\n" + sei.getErrorMessage();
                } else {
                    eStatus = sei.getStatus().toString().toUpperCase();
                }
                String response = "-";
                String duration = "-";
                if (sei.getResponse() != null) {
                    response = sei.getResponse().getStatus().toString().toUpperCase();
                    if (sei.getResponse().getStatus() == StepResponse.StepStatus.ERRONEOUS) {
                        response += "\n" + sei.getResponse().getErrorMessage();
                    }
                    if (sei.getResponse().getDuration() != null) {
                        duration = formatDuration(sei.getResponse().getDuration());
                    }
                }
                data[i] = new String[]{
                        sei.getStep(),
                        eStatus,
                        response,
                        duration
                };
            }
            return FlipTableConverters.fromObjects(headers, data);
        }
    }
}
