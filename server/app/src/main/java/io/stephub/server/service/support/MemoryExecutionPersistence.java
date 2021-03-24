package io.stephub.server.service.support;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.stephub.provider.api.model.LogEntry;
import io.stephub.provider.api.model.StepResponse;
import io.stephub.server.api.model.Execution;
import io.stephub.server.api.model.FunctionalExecution;
import io.stephub.server.api.model.FunctionalExecution.FeatureExecutionItem;
import io.stephub.server.api.model.FunctionalExecution.ScenarioExecutionItem;
import io.stephub.server.api.model.FunctionalExecution.StepExecutionItem;
import io.stephub.server.api.model.RuntimeSession.SessionSettings.ParallelizationMode;
import io.stephub.server.api.model.Workspace;
import io.stephub.server.service.ExecutionPersistence;
import io.stephub.server.service.SessionService;
import io.stephub.server.service.exception.ExecutionException;
import lombok.Builder;
import lombok.Getter;
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
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static io.stephub.server.api.model.Execution.ExecutionStatus.*;

@Service
@Slf4j
public class MemoryExecutionPersistence implements ExecutionPersistence {
    @Value("${io.stephub.execution.memory.store.size}")
    private int storeSize;

    @Value("${io.stephub.execution.memory.store.expirationDays}")
    private int expirationDays;

    private ExpiringMap<String, ExecutionWrapper> store;

    @JsonIgnore
    private final Map<String, LogEntry.LogAttachment> attachmentStore = new HashMap<>();


    @Autowired
    private SessionService sessionService;

    @Getter
    private static class ExecutionWrapper<E extends Execution> {
        private final E execution;
        @JsonIgnore
        private final Map<String, LogEntry.LogAttachment> attachmentStore = new HashMap<>();

        public ExecutionWrapper(final E execution) {
            this.execution = execution;
        }
    }

    @Builder
    private static class IsolatedBucket {
        private final FunctionalExecution.ExecutionItem source;
        private final Queue<StepExecutionItem> pendingSteps;
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
        final List<FunctionalExecution.ExecutionItem> executionItems = executionStart.getInstruction().buildItems(workspace);
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
                initiatedAt(new Date()).
                backlog(executionItems).
                pendingBuckets(pendingItems).
                uncompletedBuckets(pendingItems.size()).
                build();
        this.store.put(this.getStoreId(workspace.getId(), execution.getId()),
                new ExecutionWrapper<>(execution));
        return execution;
    }

    @Override
    public <E extends Execution> E initExecution(final Workspace workspace, final Execution.ExecutionStart<E> executionStart) {
        if (executionStart instanceof FunctionalExecution.FunctionalExecutionStart) {
            return (E) this.initExecution(workspace, (FunctionalExecution.FunctionalExecutionStart) executionStart);
        }
        return null;
    }

    private SerialBucket doLifecycleAndGetNext(final MemoryFunctionalExecution execution) {
        synchronized (execution) {
            if (execution.getStatus() == INITIATED) {
                log.debug("Execution={} started", execution);
                execution.setStatus(EXECUTING);
                execution.setStartedAt(new Date());
            }
            final SerialBucket next = execution.pendingBuckets.poll();
            if (next != null) {
                log.debug("Next bucket={} in execution={}", next, execution);
                return next;
            }
            if (execution.uncompletedBuckets > 0) {
                return null;
            }
            execution.setStatus(COMPLETED);
            execution.setCompletedAt(new Date());
            log.debug("Execution={} completed", execution);
            execution.notifyAll();
            return null;
        }
    }

    @Override
    public void processPendingExecutionSteps(final Workspace workspace, final String execId, final WithinExecutionStepCommand command) {
        final ExecutionWrapper<MemoryFunctionalExecution> executionWrapper = this.getSafeExecution(workspace.getId(), execId, MemoryFunctionalExecution.class);
        final MemoryFunctionalExecution execution = executionWrapper.getExecution();
        SerialBucket serialBucket;
        while ((serialBucket = this.doLifecycleAndGetNext(execution)) != null) {
            try {
                IsolatedBucket isolatedBucket;
                while ((isolatedBucket = serialBucket.serialBuckets.poll()) != null) {
                    try {
                        final IsolatedBucket fisb = isolatedBucket;
                        this.sessionService.doWithinIsolatedSession(workspace, execution.getSessionSettings(),
                                (session, sessionExecutionContext, evaluationContext) -> {
                                    StepExecutionItem stepExecutionItem;
                                    boolean cancelled = false;
                                    while ((stepExecutionItem = fisb.pendingSteps.poll()) != null) {
                                        if (cancelled) {
                                            log.debug("Cancel execution of step item={} in execution={} due to previous errors", stepExecutionItem, execId);
                                            stepExecutionItem.setStatus(CANCELLED);
                                            continue;
                                        }
                                        stepExecutionItem.setStatus(EXECUTING);
                                        log.debug("Starting execution of step item={} of execution={}", stepExecutionItem, execId);
                                        try {
                                            final StepExecutionResult result = command.execute(stepExecutionItem, sessionExecutionContext, evaluationContext);
                                            stepExecutionItem.setResponse(new FunctionalExecution.RoughStepResponse(result.getResponse(), this.storeLogs(executionWrapper, stepExecutionItem, result.getResponse().getLogs())));
                                            stepExecutionItem.setStepSpec(result.getStepSpec());
                                            if (result.getResponse().getStatus() != StepResponse.StepStatus.PASSED) {
                                                cancelled = true;
                                            }
                                        } catch (final Exception e) {
                                            log.warn("Failed to proceed step item={} on workspace={} and execution={}", stepExecutionItem, workspace.getId(), execId, e);
                                            stepExecutionItem.setErroneous(true);
                                            stepExecutionItem.setErrorMessage(e.getMessage());
                                            cancelled = true;
                                        } finally {
                                            stepExecutionItem.setStatus(COMPLETED);
                                            log.debug("Completed execution of step item={} of execution={}", stepExecutionItem, execId);
                                        }
                                    }
                                });
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

    private List<Execution.ExecutionLogEntry> storeLogs(final ExecutionWrapper<? extends Execution> execution, final StepExecutionItem stepExecutionItem, final List<LogEntry> logs) {
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

    private String getStoreId(final String wid, final String execId) {
        return wid + "/" + execId;
    }
}
