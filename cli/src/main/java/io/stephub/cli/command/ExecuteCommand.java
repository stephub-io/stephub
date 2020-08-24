package io.stephub.cli.command;

import com.jakewharton.fliptables.FlipTableConverters;
import io.stephub.cli.client.ExecutionClient;
import io.stephub.cli.client.WorkspaceClient;
import io.stephub.provider.api.model.StepResponse;
import io.stephub.server.api.model.Execution;
import io.stephub.server.api.model.Execution.FeatureExecutionItem;
import io.stephub.server.api.model.Execution.ScenarioExecutionItem;
import io.stephub.server.api.model.ExecutionInstruction;
import io.stephub.server.api.model.ExecutionInstruction.NamedFeatureFilter;
import io.stephub.server.api.model.ExecutionInstruction.NamedScenarioFilter;
import io.stephub.server.api.model.ExecutionInstruction.ScenariosExecutionInstruction;
import io.stephub.server.api.model.ExecutionInstruction.TagFilter;
import io.stephub.server.api.model.Workspace;
import lombok.extern.slf4j.Slf4j;
import me.tongfei.progressbar.DelegatingProgressBarConsumer;
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import picocli.CommandLine;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static io.stephub.provider.api.model.StepResponse.StepStatus.*;
import static io.stephub.server.api.model.Execution.ExecutionStatus.COMPLETED;
import static picocli.CommandLine.Help.Visibility.ALWAYS;

@Component
@CommandLine.Command(name = "execute", mixinStandardHelpOptions = true,
        exitCodeOnExecutionException = 34,
        subcommands = {
                ExecuteCommand.ExecuteStepsCommand.class,
                ExecuteCommand.ExecuteScenariosCommand.class
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

        @CommandLine.Option(names = {"-n"}, description = "Number of parallel execution sessions", required = true, defaultValue = "1", showDefaultValue = ALWAYS)
        protected int parallelSessionCount;

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

        protected String formatExecutionStatus(final Execution.ExecutionItem ei) {
            final String eStatus;
            if (ei.isErroneous()) {
                eStatus = "ERRONEOUS\n" + ei.getErrorMessage();
            } else {
                eStatus = ei.getStatus().toString().toUpperCase();
            }
            return eStatus;
        }
    }

    @Component
    @CommandLine.Command(name = "steps", mixinStandardHelpOptions = true,
            exitCodeOnExecutionException = 34)
    @Slf4j
    public static class ExecuteStepsCommand extends ExecuteBaseCommand implements Runnable {
        @CommandLine.Parameters(arity = "1..*", paramLabel = "STEP", description = "Step to execute")
        private List<String> steps;


        @Override
        public void run() {
            final Workspace foundWorkspace = this.workspaceClient.findWorkspace(this.getServerContext(), this.workspace, true);
            log.info("Start execution of steps: {}", this.steps);
            final AtomicReference<ProgressBar> progressBar = new AtomicReference<>();
            final Execution execution = this.executionClient.executeAndWaitForCompletion(this.getServerContext(), foundWorkspace,
                    Execution.ExecutionStart.builder().instruction(
                            ExecutionInstruction.StepsExecutionInstruction.builder().
                                    steps(this.steps).build()
                    ).parallelSessionCount(this.parallelSessionCount).build(),
                    changedExecution -> {
                        if (progressBar.get() == null) {
                            progressBar.set(new ProgressBarBuilder()
                                    .setInitialMax(this.steps.size())
                                    .setTaskName("Executed steps")
                                    .setConsumer(new DelegatingProgressBarConsumer(log::info))
                                    .build());
                        }
                        progressBar.get().stepTo(changedExecution.getBacklog().stream().
                                filter(executionItem -> executionItem.getStatus() == COMPLETED).
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
                String response = "-";
                String duration = "-";
                if (sei.getResponse() != null) {
                    response = sei.getResponse().getStatus().toString().toUpperCase();
                    if (sei.getResponse().getStatus() == StepResponse.StepStatus.ERRONEOUS) {
                        response += "\n" + sei.getResponse().getErrorMessage();
                    }
                    duration = formatDuration(sei.getResponse().getDuration());
                }
                data[i] = new String[]{
                        sei.getStep(),
                        this.formatExecutionStatus(sei),
                        response,
                        duration
                };
            }
            return FlipTableConverters.fromObjects(headers, data);
        }
    }

    @Component
    @CommandLine.Command(name = "scenarios", mixinStandardHelpOptions = true,
            exitCodeOnExecutionException = 34)
    @Slf4j
    public static class ExecuteScenariosCommand extends ExecuteBaseCommand implements Runnable {
        @CommandLine.ArgGroup(exclusive = true)
        private FilterOptions filterOptions;

        private static class FilterOptions {
            @CommandLine.Option(names = {"--scenario"}, arity = "0..*", description = "Filter by scenario name as regex pattern")
            private List<String> filterScenarioNames;

            @CommandLine.Option(names = {"--feature"}, arity = "0..*", description = "Filter by feature name as regex pattern")
            private List<String> filterFeatureNames;

            @CommandLine.Option(names = {"--tag"}, arity = "0..*", description = "Filter by tag name as regex pattern")
            private List<String> filterTags;

            ExecutionInstruction.ScenarioFilter buildFilter() {
                if (this.filterScenarioNames != null) {
                    return NamedScenarioFilter.builder().patterns(this.filterScenarioNames).build();
                } else if (this.filterFeatureNames != null) {
                    return NamedFeatureFilter.builder().patterns(this.filterFeatureNames).build();
                } else if (this.filterTags != null) {
                    return TagFilter.builder().patterns(this.filterTags).build();
                }
                throw new IllegalArgumentException("Unknown filter passed");
            }
        }

        @Override
        public void run() {
            final Workspace foundWorkspace = this.workspaceClient.findWorkspace(this.getServerContext(), this.workspace, true);
            log.info("Start execution of scenarios");
            final AtomicReference<ProgressBar> progressBar = new AtomicReference<>();
            final Execution execution = this.executionClient.executeAndWaitForCompletion(this.getServerContext(), foundWorkspace,
                    Execution.ExecutionStart.builder().instruction(
                            ScenariosExecutionInstruction.builder().filter(
                                    this.filterOptions != null ? this.filterOptions.buildFilter() : new ExecutionInstruction.AllScenarioFilter()
                            ).build()
                    ).parallelSessionCount(this.parallelSessionCount).build(),
                    changedExecution -> {
                        final List<ScenarioExecutionItem> scenarioExecutionItems = this.getFlattedScenarios(changedExecution.getBacklog());
                        if (progressBar.get() == null) {
                            progressBar.set(new ProgressBarBuilder()
                                    .setInitialMax(scenarioExecutionItems.size())
                                    .setTaskName("Executed scenarios")
                                    .setConsumer(new DelegatingProgressBarConsumer(log::info))
                                    .build());
                        }
                        progressBar.get().stepTo(scenarioExecutionItems.stream().
                                filter(item -> item.getStatus() == COMPLETED).
                                count());
                    }
            );
            if (progressBar.get() != null) {
                progressBar.get().stepTo(this.getFlattedScenarios(execution.getBacklog()).stream().
                        filter(item -> item.getStatus() == COMPLETED).
                        count());
                progressBar.get().close();
            }
            if (execution.isErroneous()) {
                log.error("Execution failed: {}\n{}", execution.getErrorMessage(), this.formatResult(execution));
            } else {
                log.info("Execution completed successfully\n{}", this.formatResult(execution));
            }
        }

        private Object formatResult(final Execution execution) {
            final String[] headers = {"Feature", "Scenario", "Execution", "Status", "Duration"};
            final int c = execution.getBacklog().size();
            final List<String[]> data = new ArrayList<>();
            for (int i = 0; i < c; i++) {
                if (!(execution.getBacklog().get(i) instanceof FeatureExecutionItem)) {
                    log.warn("Skipped result of non feature item: {}", execution.getBacklog().get(i));
                    continue;
                }
                final FeatureExecutionItem fei = (FeatureExecutionItem) execution.getBacklog().get(i);
                for (final ScenarioExecutionItem sei : fei.getScenarios()) {
                    data.add(new String[]{
                            fei.getName(),
                            sei.getName(),
                            this.formatExecutionStatus(sei),
                            this.formatStatus(sei).toUpperCase(),
                            this.formatDuration(sei)
                    });
                }
            }
            return FlipTableConverters.fromObjects(headers, data.toArray(new String[data.size()][]));
        }

        private String formatDuration(final ScenarioExecutionItem sei) {
            final List<Duration> durations = sei.getSteps().stream().map(Execution.StepExecutionItem::getResponse).
                    map(response -> response != null ? response.getDuration() : Duration.ZERO).
                    collect(Collectors.toList());
            Duration duration = Duration.ofMinutes(0);
            for (final Duration dur : durations) {
                duration = duration.plus(dur);
            }
            return formatDuration(duration);
        }

        private String formatStatus(final ScenarioExecutionItem sei) {
            final List<StepResponse.StepStatus> statuses = sei.getSteps().stream().map(stepExecutionItem -> stepExecutionItem.getResponse()).
                    map(response -> response != null ? response.getStatus() : null).
                    collect(Collectors.toList());
            if (statuses.contains(FAILED)) {
                return FAILED.toString();
            } else if (statuses.contains(ERRONEOUS)) {
                return ERRONEOUS.toString();
            } else if (statuses.contains(null)) {
                return "---";
            } else {
                return PASSED.toString();
            }
        }


        private List<ScenarioExecutionItem> getFlattedScenarios(final List<Execution.ExecutionItem> backlog) {
            final List<ScenarioExecutionItem> scenarios = new ArrayList<>();
            backlog.stream().forEach(executionItem -> {
                if (executionItem instanceof ScenarioExecutionItem) {
                    scenarios.add((ScenarioExecutionItem) executionItem);
                } else if (executionItem instanceof FeatureExecutionItem) {
                    scenarios.addAll(((FeatureExecutionItem) executionItem).getScenarios());
                }
            });
            return scenarios;
        }
    }
}
