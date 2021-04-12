package io.stephub.server.service.support;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.stephub.json.Json;
import io.stephub.provider.api.model.LogEntry;
import io.stephub.provider.api.model.StepResponse;
import io.stephub.server.api.StepExecution;
import io.stephub.server.api.model.*;
import io.stephub.server.api.model.Execution.*;
import io.stephub.server.api.model.LoadExecution.LoadRunner;
import io.stephub.server.api.model.LoadExecution.LoadSimulation;
import io.stephub.server.api.model.LoadExecution.RunnerStatus;
import io.stephub.server.api.model.RuntimeSession.SessionSettings.ParallelizationMode;
import io.stephub.server.service.ExecutionPersistence;
import io.stephub.server.service.SessionService;
import io.stephub.server.service.StepExecutionResolver;
import io.stephub.server.service.exception.ExecutionException;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import net.jodah.expiringmap.ExpirationPolicy;
import net.jodah.expiringmap.ExpiringMap;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static io.stephub.provider.api.model.StepResponse.StepStatus.*;
import static io.stephub.server.api.model.Execution.ExecutionStatus.*;
import static io.stephub.server.api.model.Fixture.FixtureType.AFTER;
import static io.stephub.server.api.model.Fixture.FixtureType.BEFORE;

@Service
@Slf4j
public class MemoryExecutionPersistence implements ExecutionPersistence {
    @Value("${io.stephub.execution.memory.store.size}")
    private int storeSize;

    @Value("${io.stephub.execution.memory.store.expirationDays}")
    private int expirationDays;

    private ExpiringMap<String, ExecutionWrapper> store;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private StepExecutionResolver stepExecutionResolver;

    @Getter
    private static class ExecutionWrapper<E extends Execution> {
        private final E execution;
        private final Map<String, LogEntry.LogAttachment> attachmentStore = new HashMap<>();
        private final List<LoadExecution.LoadScenarioRun> loadRuns = Collections.synchronizedList(new ArrayList<>());

        public ExecutionWrapper(final E execution) {
            this.execution = execution;
        }
    }

    @Builder
    private static class IsolatedBucket {
        private final FunctionalExecution.ExecutionItem source;
        private final List<StepExecutionItem> pendingSteps;
    }

    @Builder
    private static class SerialBucket {
        private final FunctionalExecution.ExecutionItem source;
        private final Queue<IsolatedBucket> serialBuckets;
    }


    @SuperBuilder
    @Getter
    @JsonTypeName(Execution.FUNCTIONAL_STR)
    private static class MemoryFunctionalExecution extends FunctionalExecution {
        @JsonIgnore
        private final Queue<SerialBucket> pendingBuckets;

        @JsonIgnore
        private int uncompletedBuckets;

        @JsonIgnore
        private final Map<String, Json> fixturePresets = new HashMap<>();

        @Override
        @JsonIgnore
        public int getMaxParallelizationCount() {
            return this.pendingBuckets.size();
        }
    }

    @PostConstruct
    public void setUp() {
        this.store = ExpiringMap.builder()
                .expiration(this.expirationDays, TimeUnit.DAYS)
                .expirationPolicy(ExpirationPolicy.CREATED)
                .maxSize(this.storeSize)
                .build();
    }

    private FunctionalExecution initExecution(final Workspace workspace, final FunctionalExecution.FunctionalExecutionStart executionStart) {
        final Set<Fixture> matchingFixtures = new HashSet<>();
        final List<FunctionalExecution.ExecutionItem> executionItems = executionStart.getInstruction().buildItems(workspace,
                matchingFixtures::add);
        final Queue<SerialBucket> pendingItems = new LinkedList<>();
        executionItems.forEach(executionItem ->
        {
            if (executionItem instanceof StepExecutionItem) {
                pendingItems.add(SerialBucket.builder().serialBuckets(
                        new LinkedList<>(Collections.singleton(
                                IsolatedBucket.builder().pendingSteps(
                                        new LinkedList<>(Collections.singleton((StepExecutionItem) executionItem))
                                ).build()
                        ))
                ).build());
            } else if (executionItem instanceof ScenarioExecutionItem) {
                pendingItems.add(SerialBucket.builder().serialBuckets(
                        new LinkedList<>(Collections.singleton(
                                IsolatedBucket.builder().pendingSteps(
                                        new LinkedList<>(
                                                ((ScenarioExecutionItem) executionItem).getSteps()
                                        )
                                ).build()
                        ))).build());
            } else if (executionItem instanceof FeatureExecutionItem) {
                if (executionStart.getParallelizationMode() == ParallelizationMode.SCENARIO) {
                    pendingItems.addAll(
                            ((FeatureExecutionItem) executionItem).getScenarios().stream()
                                    .map(scenario -> SerialBucket.builder().serialBuckets(
                                            new LinkedList<>(Collections.singleton(
                                                    IsolatedBucket.builder().pendingSteps(
                                                            new LinkedList<>(
                                                                    scenario.getSteps()
                                                            )
                                                    ).build()
                                            ))).build()).collect(Collectors.toList()));
                } else {
                    pendingItems.add(SerialBucket.builder().serialBuckets(
                            new LinkedList<>(
                                    ((FeatureExecutionItem) executionItem).getScenarios().stream().map(
                                            scenario -> IsolatedBucket.builder().pendingSteps(
                                                    new LinkedList<>(
                                                            scenario.getSteps()
                                                    )
                                            ).build()
                                    ).collect(Collectors.toList())
                            )).build());
                }
            }
        });
        if (pendingItems.isEmpty()) {
            throw new ExecutionException("Empty selection, expected at least one item to execute");
        }
        final MemoryFunctionalExecution execution = MemoryFunctionalExecution.builder().
                instruction(executionStart.getInstruction()).
                id(UUID.randomUUID().toString()).
                sessionSettings(executionStart.getSessionSettings()).
                gherkinPreferences(workspace.getGherkinPreferences()).
                initiatedAt(OffsetDateTime.now()).
                backlog(executionItems).
                pendingBuckets(pendingItems).
                uncompletedBuckets(pendingItems.size()).
                fixtures(matchingFixtures.stream().map(FixtureExecutionItem::new)
                        .sorted().collect(Collectors.toList())).
                build();
        this.store.put(this.getStoreId(workspace.getId(), execution.getId()),
                new ExecutionWrapper<>(execution));
        return execution;
    }

    private LoadExecution initExecution(final Workspace workspace, final LoadExecution.LoadExecutionStart executionStart) {
        final List<LoadSimulation> simulations = new ArrayList<>();
        final MemoryLoadExecution.MemoryLoadExecutionBuilder<?, ?> executionBuilder = MemoryLoadExecution.builder()
                .id(UUID.randomUUID().toString())
                .start(executionStart)
                .sessionSettings(executionStart.getSessionSettings())
                .gherkinPreferences(workspace.getGherkinPreferences())
                .initiatedAt(OffsetDateTime.now())
                .status(INITIATED)
                .simulations(simulations);
        executionStart.getSimulations().stream().forEach(
                simulationSpec -> {
                    final MemoryLoadSimulation.MemoryLoadSimulationBuilder<?, ?> simBuilder = MemoryLoadSimulation.builder().name(
                            simulationSpec.getName()
                    ).userLoadSpec(
                            simulationSpec.getUserLoad()
                    );
                    final List<LoadExecution.LoadScenario> loadScenarios = new ArrayList<>();
                    final Set<Fixture> matchingFixtures = new HashSet<>();
                    simulationSpec.getSelection().filter(workspace, (feature, scenario, steps) ->
                            {
                                loadScenarios.add(
                                        LoadExecution.LoadScenario.builder().featureName(feature.getName()).
                                                name(scenario.getName()).
                                                stats(new MemoryLoadStats()).
                                                steps(
                                                        steps.stream().map(step ->
                                                                LoadExecution.LoadStep.builder().
                                                                        step(step).stats(new MemoryLoadStats()).build()
                                                        ).collect(Collectors.toList())
                                                ).build());
                                matchingFixtures.addAll(
                                        workspace.getFixtures().stream().filter(f -> f.appliesTo(feature, scenario)).collect(Collectors.toList())
                                );
                            }
                    );
                    if (loadScenarios.isEmpty()) {
                        throw new ExecutionException("Empty scenario selection in simulation: " + simulationSpec.getName());
                    }
                    simBuilder.scenarios(loadScenarios);
                    simBuilder.fixtureTemplates(matchingFixtures.stream().map(
                            FixtureExecutionItem::new
                    ).sorted().collect(Collectors.toList()));
                    simulations.add(simBuilder.build());
                }
        );
        final MemoryLoadExecution execution = executionBuilder.build();
        this.store.put(this.getStoreId(workspace.getId(), execution.getId()),
                new ExecutionWrapper<>(execution));
        return execution;
    }

    @Override
    public <E extends Execution> E initExecution(final Workspace workspace, final Execution.ExecutionStart<E> executionStart) {
        if (executionStart instanceof FunctionalExecution.FunctionalExecutionStart) {
            return (E) this.initExecution(workspace, (FunctionalExecution.FunctionalExecutionStart) executionStart);
        } else if (executionStart instanceof LoadExecution.LoadExecutionStart) {
            return (E) this.initExecution(workspace, (LoadExecution.LoadExecutionStart) executionStart);
        }
        return null;
    }


    private SerialBucket doLifecycleAndGetNext(final MemoryFunctionalExecution execution, final Runnable startFixturesExecutor,
                                               final Runnable stopFixturesExecutor) {
        synchronized (execution) {
            if (execution.getStatus() == INITIATED) {
                log.debug("Execution={} started", execution);
                execution.setStatus(EXECUTING);
                execution.setStartedAt(OffsetDateTime.now());
                startFixturesExecutor.run();
            }
            final SerialBucket next = execution.pendingBuckets.poll();
            if (next != null) {
                log.debug("Next bucket={} in execution={}", next, execution);
                return next;
            }
            if (execution.uncompletedBuckets > 0) {
                return null;
            }
            stopFixturesExecutor.run();
            execution.setStatus(COMPLETED);
            execution.setCompletedAt(OffsetDateTime.now());
            log.debug("Execution={} completed", execution);
            execution.notifyAll();
            return null;
        }
    }

    private class ExecutionStepItemResponseContext implements StepResponseContext {
        ExecutionWrapper<? extends Execution> executionWrapper;
        private final StepExecutionItem executionItem;
        private boolean continuable = true;

        public ExecutionStepItemResponseContext(final ExecutionWrapper<? extends Execution> executionWrapper, final StepExecutionItem executionItem) {
            this.executionWrapper = executionWrapper;
            this.executionItem = executionItem;
        }

        @Override
        public void completeStep(final StepResponse<Json> response) {
            this.continuable = response.getStatus() == PASSED;
            this.executionItem.setResult(new Execution.StepItemResultLeaf(response,
                    MemoryExecutionPersistence.this.storeLogs(this.executionWrapper, response.getLogs()))
            );
        }

        @Override
        public NestedResponseContext nested() {
            final StepItemResultNested resultNested = new StepItemResultNested();
            this.executionItem.setResult(resultNested);
            return new NestedResponseContext() {
                @Override
                public NestedResponseSequenceContext group(final Optional<String> name) {
                    final StepItemResultGroup resultGroup = new StepItemResultGroup();
                    resultGroup.setName(name.get());
                    resultNested.getGroups().add(resultGroup);
                    return new NestedResponseSequenceContext() {
                        private final List<StepResponseContext> subContexts = new ArrayList<>();

                        @Override
                        public StepResponseContext startStep(final String step) {
                            final StepExecutionItem item = Execution.StepExecutionItem.builder().step(step).build();
                            resultGroup.getSteps().add(item);
                            final StepResponseContext stepSubContext = new ExecutionStepItemResponseContext(ExecutionStepItemResponseContext.this.executionWrapper, item);
                            this.subContexts.add(stepSubContext);
                            return stepSubContext;
                        }

                        @Override
                        public boolean continuable() {
                            boolean c = true;
                            for (final StepResponseContext sub : this.subContexts) {
                                c &= sub.continuable();
                            }
                            return c;
                        }
                    };
                }

                @Override
                public boolean continuable() {
                    return resultNested.getStatus() == PASSED;
                }
            };
        }

        @Override
        public boolean continuable() {
            return this.continuable;
        }
    }

    @Override
    public void processPendingExecutionSteps(final Workspace workspace, final String execId, final WithinExecutionStepCommand command) {
        final ExecutionWrapper<MemoryFunctionalExecution> executionWrapper = this.getSafeExecution(workspace.getId(), execId, MemoryFunctionalExecution.class);
        final MemoryFunctionalExecution execution = executionWrapper.getExecution();
        SerialBucket serialBucket;
        final Runnable fixturesStarter = () -> {
            final boolean fixturesPassed = MemoryExecutionPersistence.this.executeFixtures(
                    workspace,
                    command, executionWrapper,
                    execution.getFixtures().stream().filter(f -> f.getType() == BEFORE).collect(Collectors.toList()),
                    execution.fixturePresets,
                    (attributes) -> execution.fixturePresets.putAll(attributes)
            );
            if (!fixturesPassed) {
                throw new ExecutionException("Execution aborted due to erroneous fixtures");
            }
        };
        final Runnable fixturesStopper = () -> MemoryExecutionPersistence.this.executeFixtures(
                workspace,
                command, executionWrapper,
                execution.getFixtures().stream().filter(f -> f.getType() == AFTER).collect(Collectors.toList()),
                execution.fixturePresets, null);

        while ((serialBucket = this.doLifecycleAndGetNext(execution, fixturesStarter, fixturesStopper)) != null) {
            try {
                IsolatedBucket isolatedBucket;
                while ((isolatedBucket = serialBucket.serialBuckets.poll()) != null) {
                    try {
                        this.executeStepSequenceIsolated(workspace, command, executionWrapper, isolatedBucket.pendingSteps, execution.fixturePresets, null);
                    } catch (final Exception e) {
                        log.warn("Failed to proceed isolated bucket={} on workspace={} and execution={}", isolatedBucket, workspace.getId(), execId, e);
                        isolatedBucket.source.setErroneous(true);
                        isolatedBucket.source.setErrorMessage(e.getMessage());
                    }
                }
            } catch (final Exception e) {
                log.warn("Failed to proceed serial bucket={} on workspace={} and execution={}", serialBucket, workspace.getId(), execId, e);
                serialBucket.source.setErroneous(true);
                serialBucket.source.setErrorMessage(e.getMessage());
            } finally {
                synchronized (execution) {
                    execution.uncompletedBuckets--;
                }
            }
        }
    }

    private List<Execution.ExecutionLogEntry> storeLogs(final ExecutionWrapper<? extends Execution> execution, final List<LogEntry> logs) {
        if (!CollectionUtils.isEmpty(logs)) {
            return logs.stream().map(logEntry -> Execution.ExecutionLogEntry.builder()
                    .message(logEntry.getMessage()).attachments(
                            logEntry.getAttachments().stream().map(logAttachment -> {
                                final Execution.ExecutionLogAttachment target =
                                        new Execution.ExecutionLogAttachment(UUID.randomUUID().toString(), logAttachment);
                                execution.attachmentStore.put(target.getId(), logAttachment);
                                return target;
                            }).collect(Collectors.toList())
                    ).build()).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    private void deleteLogs(final ExecutionWrapper<? extends Execution> execution, final StepExecutionItem item) {
        if (item.getResult() != null && item.getResult().getLogs() != null) {
            item.getResult().getLogs().forEach(log -> {
                if (log.getAttachments() != null) {
                    log.getAttachments().forEach(attachment -> execution.attachmentStore.remove(attachment.getId()));
                }
            });
        }
    }

    @Override
    public Execution getExecution(final String wid, final String execId) {
        return this.getSafeExecution(wid, execId, Execution.class).getExecution();
    }

    private <E extends Execution> ExecutionWrapper<E> getSafeExecution(final String wid, final String execId, final Class<E> clazz) {
        final ExecutionWrapper<E> execution = this.store.get(this.getStoreId(wid, execId));
        if (execution == null) {
            throw new ExecutionException("No execution with id=" + execId + " and workspace=" + wid + " found");
        }
        return execution;
    }

    @Override
    public CompletableFuture<Execution> getExecution(final String wid, final String execId, final boolean waitForCompletion) {
        final Execution execution = this.getSafeExecution(wid, execId, Execution.class).getExecution();
        try {
            while (waitForCompletion && execution.getStatus() != Execution.ExecutionStatus.COMPLETED) {
                synchronized (execution) {
                    execution.wait(10000);
                }
            }
        } catch (final InterruptedException e) {
            return CompletableFuture.failedFuture(e);
        }
        return CompletableFuture.completedFuture(execution);
    }

    @Override
    public <E extends Execution> List<E> getExecutions(final String wid, final Class<? extends E> clazz) {
        return this.store.entrySet().stream().filter(e -> e.getKey().startsWith(wid + "/")).
                filter(e -> clazz.isInstance(e.getValue().getExecution())).
                map(e -> clazz.cast(e.getValue().getExecution())).sorted((e1, e2) -> e2.getInitiatedAt().compareTo(e1.getInitiatedAt())).
                collect(Collectors.toList());
    }

    @Override
    public Pair<Execution.ExecutionLogAttachment, InputStream> getLogAttachment(final String wid, final String execId, final String attachmentId) {
        final LogEntry.LogAttachment logAttachment = this.getSafeExecution(wid, execId, Execution.class).attachmentStore.get(attachmentId);
        if (logAttachment == null) {
            throw new ExecutionException("No attachment with id=" + attachmentId + " found");
        }
        return Pair.of(new Execution.ExecutionLogAttachment(attachmentId, logAttachment), new ByteArrayInputStream(logAttachment.getContent()));
    }

    @SuperBuilder
    @JsonTypeName(Execution.LOAD_STR)
    private static class MemoryLoadExecution extends LoadExecution {

    }


    @SuperBuilder
    private static class MemoryLoadSimulation extends LoadSimulation {
        @JsonIgnore
        @Builder.Default
        private final List<LoadRunner> runners = new ArrayList<>();

        @JsonIgnore
        @Builder.Default
        private final List<FixtureExecutionItem> fixtureTemplates = new ArrayList<>();

        @Override
        public int getCurrentTargetLoad() {
            return (int) this.runners.stream().filter(r -> r.getStatus().alive()).count();
        }

        @Override
        public int getCurrentActualLoad() {
            return (int) this.runners.stream().filter(r -> r.getStatus() == RunnerStatus.RUNNING).count();
        }

        @Override
        public List<LoadRunner> getRunners() {
            return Collections.unmodifiableList(this.runners);
        }
    }

    @NoArgsConstructor
    private static class MemoryLoadStats extends LoadExecution.LoadStats {
        @JsonIgnore
        private Duration totalDuration = Duration.ZERO;

    }

    @Override
    public void processLoadRunner(final Workspace workspace, final String execId, final String runnerId, final WithinExecutionStepCommand command) {
        final ExecutionWrapper<MemoryLoadExecution> wrapper = this.getSafeExecution(workspace.getId(), execId, MemoryLoadExecution.class);
        final MemoryLoadExecution execution = wrapper.getExecution();
        final Pair<MemoryLoadSimulation, LoadRunner> simRunner = this.findRunner(execution, runnerId);
        if (simRunner == null) {
            log.warn("Runner={} not found for execution={}", runnerId, execution);
            return;
        }
        final MemoryLoadSimulation simulation = simRunner.getKey();
        final LoadRunner runner = simRunner.getValue();
        final Map<String, Json> fixturePresets = new HashMap<>();
        synchronized (execution) {
            if (execution.getStatus() == INITIATED) {
                log.info("Started load execution={}", execution);
                execution.setStatus(EXECUTING);
                execution.setStartedAt(OffsetDateTime.now());
            }
            if (execution.getStatus() != EXECUTING) {
                runner.setStatus(RunnerStatus.STOPPED);
                runner.setStopMessage("Execution isn't longer executing, runner get aborted");
                return;
            }
        }
        try {
            runner.setStatus(RunnerStatus.RUNNING);
            runner.setStartedAt(OffsetDateTime.now());
            log.debug("Runner={} starts work for execution={}", runnerId, execution);
            final boolean fixturesPassed = this.executeFixtures(
                    workspace,
                    command, wrapper,
                    runner.getFixtures().stream().filter(f -> f.getType() == BEFORE).collect(Collectors.toList()),
                    fixturePresets,
                    (attributes) -> fixturePresets.putAll(attributes)
            );
            if (!fixturesPassed) {
                runner.setStatus(RunnerStatus.STOPPED);
                runner.setStopMessage("Execution aborted due to erroneous fixtures");
                return;
            }
            while (runner.getStatus().alive()) {
                runner.setIterationNumber(runner.getIterationNumber() + 1);
                for (final LoadExecution.LoadScenario loadScenario : simulation.getScenarios()) {
                    final List<StepExecutionItem> stepItems = loadScenario.getSteps().stream().map(
                            step -> StepExecutionItem.builder()
                                    .status(INITIATED)
                                    .step(step.getStep())
                                    .stepSpec(step.getSpec()).build()
                    ).collect(Collectors.toList());
                    final LoadExecution.LoadScenarioRun.LoadScenarioRunBuilder runBuilder = LoadExecution.LoadScenarioRun.builder().
                            scenarioId(loadScenario.getId()).
                            startedAt(OffsetDateTime.now()).
                            steps(stepItems);
                    try {
                        final boolean passed = this.executeStepSequenceIsolated(workspace, command, wrapper, stepItems, fixturePresets, null);
                        runBuilder.completedAt(OffsetDateTime.now());
                        final StepResponse.StepStatus aggStatus = this.applyStats(loadScenario, stepItems);
                        if (passed) {
                            // Delete possibly saved log attachments
                            stepItems.forEach(item -> this.deleteLogs(wrapper, item));
                            runBuilder.status(PASSED);
                        } else {
                            runBuilder.status(aggStatus);
                        }
                    } catch (final Exception e) {
                        runBuilder.completedAt(OffsetDateTime.now());
                        log.warn("Error", e);
                        log.warn("Failed to proceed load scenario={} on workspace={} and execution={}", loadScenario, workspace.getId(), execId, e);
                        runBuilder.status(ERRONEOUS);
                        runBuilder.errorMessage(e.getMessage());
                    }
                    final LoadExecution.LoadScenarioRun run = runBuilder.build();
                    if (run.getStatus() != PASSED) {
                        wrapper.getLoadRuns().add(run);
                    }
                }
                log.debug("Runner={} doing something for execution={}", runnerId, execution);
            }
        } finally {
            log.debug("Runner={} stopped work for execution={}", runnerId, execution);
            this.executeFixtures(
                    workspace,
                    command, wrapper,
                    runner.getFixtures().stream().filter(f -> f.getType() == AFTER).collect(Collectors.toList()),
                    fixturePresets, null);
            runner.setStatus(RunnerStatus.STOPPED);
            runner.setStoppedAt(OffsetDateTime.now());
            this.tryCompleting(execution);
        }
    }

    private boolean executeFixtures(final Workspace workspace, final WithinExecutionStepCommand command, final ExecutionWrapper<? extends Execution> executionWrapper, final List<FixtureExecutionItem> fixtures,
                                    final Map<String, Json> presetsInit,
                                    final Consumer<Map<String, Json>> attributesConsumer) {
        boolean cancelled = false;
        final Map<String, Json> presets = new HashMap<>(presetsInit);
        try {
            for (final FixtureExecutionItem fixture : fixtures) {
                if (cancelled) {
                    log.trace("Cancelling fixture {}", fixture);
                    fixture.getSteps().stream().forEach(step -> step.setStatus(CANCELLED));
                    continue;
                }
                log.trace("Executing fixture {}", fixture);
                final boolean passed = this.executeStepSequenceIsolated(workspace, command, executionWrapper, fixture.getSteps(),
                        presets, (attributes) -> {
                            presets.clear();
                            presets.putAll(attributes);
                        });
                if (!passed) {
                    if (fixture.isAbortOnError()) {
                        log.debug("Abort subsequent fixtures due to the failed one: {}", fixture);
                        cancelled = true;
                    }
                }
            }
        } finally {
            if (attributesConsumer != null) {
                attributesConsumer.accept(presets);
            }
        }
        return !cancelled;
    }

    private StepResponse.StepStatus applyStats(final LoadExecution.LoadScenario loadScenario, final List<StepExecutionItem> items) {
        final MemoryLoadStats stats = (MemoryLoadStats) loadScenario.getStats();
        Duration total = Duration.ZERO;
        boolean sErroneous = false;
        boolean sFailed = false;
        for (int i = 0; i < items.size(); i++) {
            final StepExecutionItem item = items.get(i);
            final MemoryLoadStats stepStats = (MemoryLoadStats) loadScenario.getSteps().get(i).getStats();
            boolean erroneous = false;
            boolean failed = false;
            if (item.isErroneous()) {
                erroneous = true;
            }
            if (item.getResult() != null) {
                final StepItemResult result = item.getResult();
                if (result.getStatus() == ERRONEOUS) {
                    erroneous = true;
                } else if (result.getStatus() == FAILED) {
                    failed = true;
                }
                total = total.plus(result.getDuration());
                this.applyStats(stepStats, erroneous, failed, item.getStatus() == CANCELLED, result.getDuration());
            } else {
                this.applyStats(stepStats, erroneous, false, item.getStatus() == CANCELLED, null);
            }
            sErroneous |= erroneous;
            sFailed |= failed;
        }
        this.applyStats(stats, sErroneous, sFailed, false, total);
        return sErroneous ? ERRONEOUS : (sFailed ? FAILED : PASSED);
    }

    private void applyStats(final MemoryLoadStats stats, final boolean erroneous, final boolean failed, final boolean cancelled, final Duration total) {
        if (erroneous) {
            stats.setErroneous(stats.getErroneous() + 1);
            return;
        } else if (cancelled) {
            stats.setCancelled(stats.getCancelled() + 1);
            return;
        } else if (failed) {
            stats.setFailed(stats.getFailed() + 1);
        } else {
            stats.setPassed(stats.getPassed() + 1);
        }
        if (total != null) {
            stats.totalDuration = stats.totalDuration.plus(total);
            if (stats.getMin() == null || stats.getMin().compareTo(total) > 0) {
                stats.setMin(total);
            }
            if (stats.getMax() == null || stats.getMax().compareTo(total) < 0) {
                stats.setMax(total);
            }
            stats.setAvg(stats.totalDuration.dividedBy(stats.getPassed() + stats.getFailed() + stats.getErroneous()));
        }
    }

    private boolean executeStepSequenceIsolated(final Workspace workspace, final WithinExecutionStepCommand command, final ExecutionWrapper<? extends Execution> executionWrapper, final List<StepExecutionItem> stepItems,
                                                final Map<String, Json> presetAttributes,
                                                final Consumer<Map<String, Json>> sessionAttributesExporter) {
        final Execution execution = executionWrapper.getExecution();
        final String execId = execution.getId();
        final AtomicBoolean passed = new AtomicBoolean(true);
        this.sessionService.doWithinIsolatedSession(workspace, executionWrapper.getExecution().getSessionSettings(), presetAttributes,
                (session, sessionExecutionContext, evaluationContext) -> {
                    boolean cancelled = false;
                    for (int i = 0; i < stepItems.size(); i++) {
                        final StepExecutionItem stepExecutionItem = stepItems.get(i);
                        if (cancelled) {
                            log.debug("Cancel execution of step item={} in execution={} due to previous errors", stepExecutionItem, execId);
                            stepExecutionItem.setStatus(CANCELLED);
                            continue;
                        }
                        stepExecutionItem.setStatus(EXECUTING);
                        log.debug("Starting execution of step item={} of execution={}", stepExecutionItem, execId);
                        try {
                            final StepExecution stepExecution = this.stepExecutionResolver.resolveAlways(stepExecutionItem.getStep(), workspace);
                            stepExecutionItem.setStepSpec(stepExecution.getStepSpec());
                            command.execute(stepExecutionItem, stepExecution, sessionExecutionContext, evaluationContext,
                                    new ExecutionStepItemResponseContext(executionWrapper, stepExecutionItem));
                            if (stepExecutionItem.getResult() == null) {
                                log.error("Missing result propagated for step item={}", stepExecutionItem);
                                cancelled = true;
                                passed.set(false);
                            } else if (stepExecutionItem.getResult().getStatus() != PASSED) {
                                cancelled = true;
                                passed.set(false);
                            }
                        } catch (final Exception e) {
                            log.warn("Failed to proceed step item={} on workspace={} and execution={}", stepExecutionItem, workspace.getId(), execId, e);
                            stepExecutionItem.setErroneous(true);
                            stepExecutionItem.setErrorMessage(e.getMessage());
                            cancelled = true;
                            passed.set(false);
                        } finally {
                            stepExecutionItem.setStatus(COMPLETED);
                            if (sessionAttributesExporter != null) {
                                sessionAttributesExporter.accept(session.getAttributes());
                            }
                            log.debug("Completed execution of step item={} of execution={}", stepExecutionItem, execId);
                        }
                    }
                });
        return passed.get();
    }

    private Pair<MemoryLoadSimulation, LoadRunner> findRunner(final MemoryLoadExecution execution, final String runnerId) {
        for (final LoadSimulation simulation : execution.getSimulations()) {
            for (final LoadRunner runner : ((MemoryLoadSimulation) simulation).runners) {
                if (runnerId.equals(runner.getId())) {
                    return Pair.of((MemoryLoadSimulation) simulation, runner);
                }
            }
        }
        return null;
    }

    @Override
    public void stopExecution(final String wid, final String execId) {
        // TODO: Generalize for both types
        final ExecutionWrapper<MemoryLoadExecution> wrapper = this.getSafeExecution(wid, execId, MemoryLoadExecution.class);
        final MemoryLoadExecution execution = wrapper.getExecution();
        synchronized (execution) {
            execution.setStatus(STOPPING);
            execution.getSimulations().forEach(loadSimulation ->
                    ((MemoryLoadSimulation) loadSimulation).runners.forEach(runner -> {
                        runner.setStatus(RunnerStatus.STOPPING);
                    })
            );
        }
        this.tryCompleting(execution);
    }

    private void tryCompleting(final MemoryLoadExecution execution) {
        synchronized (execution) {
            if (execution.getStatus() == COMPLETED || execution.getStatus() == CANCELLED) {
                return;
            }
            final int running = execution.getSimulations().stream().map(loadSimulation ->
                    ((MemoryLoadSimulation) loadSimulation).runners.stream().filter(memoryRunner -> memoryRunner.getStatus() == RunnerStatus.RUNNING).count()
            ).mapToInt(Long::intValue).sum();
            if (running == 0) {
                log.info("Completed load execution={}", execution);
                execution.setStatus(COMPLETED);
                execution.setCompletedAt(OffsetDateTime.now());
            } else {
                log.debug("Waiting completion of execution={} due to uncompleted runners={}", execution, running);
            }
        }
    }

    @Override
    public Duration adaptLoadRunners(final String wid, final String execId, final LoadRunnerSpawner loadRunnerSpawner) {
        final ExecutionWrapper<MemoryLoadExecution> wrapper = this.getSafeExecution(wid, execId, MemoryLoadExecution.class);
        final MemoryLoadExecution execution = wrapper.getExecution();
        final Duration simulationTime = execution.getStartedAt() != null ? Duration.between(execution.getStartedAt(), OffsetDateTime.now())
                : Duration.ZERO;
        final List<Duration> nextChanges = new ArrayList<>();
        execution.getSimulations().forEach(
                simulation -> {
                    final int amountNow = simulation.getUserLoadSpec().getAmountAt(simulationTime);
                    final Duration nextChangeAfter = simulation.getUserLoadSpec().nextChangeAfter(simulationTime);
                    if (nextChangeAfter != null) {
                        nextChanges.add(nextChangeAfter);
                    }
                    final int current = simulation.getCurrentTargetLoad();
                    if (amountNow > current) {
                        log.debug("Adding {} more runners to execution={}", amountNow - current, execId);
                        for (int i = 0; i < (amountNow - current); i++) {
                            final LoadRunner runner =
                                    LoadRunner.builder().id(UUID.randomUUID().toString()).
                                            initiatedAt(OffsetDateTime.now()).
                                            status(RunnerStatus.INITIATED).
                                            fixtures(
                                                    ((MemoryLoadSimulation) simulation).fixtureTemplates.stream().map(
                                                            t -> t.toBuilder().build()
                                                    ).collect(Collectors.toList())
                                            ).
                                            build();
                            ((MemoryLoadSimulation) simulation).runners.add(runner);
                            loadRunnerSpawner.spawn(runner.getId());
                        }
                    } else if (amountNow < current) {
                        log.debug("Removing {} runners from execution={}", current - amountNow, execId);
                        for (int i = 0; i < (current - amountNow); i++) {
                            ((MemoryLoadSimulation) simulation).runners.stream().filter(
                                    r -> r.getStatus().alive()
                            ).findFirst().ifPresentOrElse(
                                    runner -> {
                                        log.debug("Stopping runner={} of execution={}", runner.getId(), execId);
                                        runner.setStatus(RunnerStatus.STOPPING);
                                    },
                                    () -> log.warn("Wanted to deactivate runner, but none active found for execution={}", execId)
                            );
                        }
                    }
                }
        );
        Duration closestChange = null;
        for (final Duration nextChange : nextChanges) {
            if (closestChange == null || nextChange.compareTo(closestChange) < 0) {
                closestChange = nextChange;
            }
        }
        if (closestChange != null) {
            log.debug("Going to adapt runners for execution={} in {}", execId, closestChange);
        }
        return closestChange;
    }

    private String getStoreId(final String wid, final String execId) {
        return wid + "/" + execId;
    }
}