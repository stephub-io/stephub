package io.stephub.runtime.service.support;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.stephub.json.Json;
import io.stephub.provider.api.model.StepResponse;
import io.stephub.runtime.model.Execution;
import io.stephub.runtime.model.ExecutionInstruction;
import io.stephub.runtime.model.RuntimeSession;
import io.stephub.runtime.model.RuntimeSession.SessionSettings.ParallelizationMode;
import io.stephub.runtime.model.Workspace;
import io.stephub.runtime.service.ExecutionPersistence;
import io.stephub.runtime.service.exception.ExecutionException;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import net.jodah.expiringmap.ExpirationPolicy;
import net.jodah.expiringmap.ExpiringMap;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static io.stephub.runtime.model.Execution.ExecutionStatus.*;

@Service
@Slf4j
public class MemoryExecutionPersistence implements ExecutionPersistence {
    @Value("${io.stephub.execution.memory.store.size}")
    private int storeSize;

    @Value("${io.stephub.execution.memory.store.expirationDays}")
    private int expirationDays;

    private ExpiringMap<String, MemoryExecution> store;

    @SuperBuilder
    private static class MemoryExecution extends Execution {
        @JsonIgnore
        private final Queue<ExecutionItem> pendingItems;

        @Override
        @JsonIgnore
        public int getMaxParallelizationCount() {
            return this.pendingItems.size();
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

    @Override
    public Execution initExecution(final Workspace workspace, final ExecutionInstruction instruction, final RuntimeSession.SessionSettings sessionSettings) {
        final List<Execution.ExecutionItem> executionItems = instruction.buildItems(workspace);
        final Queue<Execution.ExecutionItem> pendingItems = new LinkedList<>();
        if (sessionSettings.getParallelizationMode() == ParallelizationMode.SCENARIO) {
            for (final Execution.ExecutionItem item : executionItems) {
                if (item instanceof Execution.FeatureExecutionItem) {
                    pendingItems.addAll(((Execution.FeatureExecutionItem) item).getScenarios());
                } else {
                    pendingItems.add(item);
                }
            }
        } else {
            pendingItems.addAll(executionItems);
        }
        final MemoryExecution execution = MemoryExecution.builder().
                instruction(instruction).
                id(UUID.randomUUID().toString()).
                sessionSettings(sessionSettings).
                initiatedAt(new Date()).
                backlog(executionItems).
                pendingItems(pendingItems).
                build();
        this.store.put(this.getStoreId(workspace.getId(), execution.getId()), execution);
        return execution;
    }

    private Execution.ExecutionItem doLifecycleAndGetNext(final MemoryExecution execution) {
        synchronized (execution) {
            if (execution.getStatus() == INITIATED) {
                log.debug("Execution={} started", execution);
                execution.setStatus(EXECUTING);
                execution.setStartedAt(new Date());
            }
            final Execution.ExecutionItem next = execution.pendingItems.poll();
            if (next != null) {
                log.debug("Next item={} in execution={}", next, execution);
                return next;
            }
            if (execution.getBacklog().stream().anyMatch(item -> item.getStatus() != COMPLETED)) {
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
    public void processPendingExecutionItems(final String wid, final String execId, final WithinExecutionCommand command) {
        final MemoryExecution execution = this.getSafeExecution(wid, execId);
        Execution.ExecutionItem executionItem;
        while ((executionItem = this.doLifecycleAndGetNext(execution)) != null) {
            try {
                command.execute(executionItem, (item, stepCommand) -> {
                    item.setStatus(EXECUTING);
                    log.debug("Starting execution of step item={} of execution={}", item, execution);
                    try {
                        final StepResponse<Json> response = stepCommand.execute();
                        item.setResponse(response);
                        return response;
                    } finally {
                        item.setStatus(COMPLETED);
                        log.debug("Completed execution of step item={} of execution={}", item, execution);
                    }
                });
            } catch (final Exception e) {
                log.warn("Failed to proceed execution item={} on workspace={} and execution={}", executionItem, wid, execId, e);
                executionItem.setErroneous(true);
                executionItem.setErrorMessage("Failed to proceed execution due to: " + e.getMessage());
            }
        }
    }

    @Override
    public Execution getExecution(final String wid, final String execId) {
        return this.getSafeExecution(wid, execId);
    }

    @NotNull
    private MemoryExecution getSafeExecution(final String wid, final String execId) {
        final MemoryExecution execution = this.store.get(this.getStoreId(wid, execId));
        if (execution == null) {
            throw new ExecutionException("No execution with id=" + execId + " and workspace=" + wid + " found");
        }
        return execution;
    }

    @Override
    public CompletableFuture<Execution> getExecution(final String wid, final String execId, final boolean waitForCompletion) {
        final Execution execution = this.getSafeExecution(wid, execId);
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
    public List<Execution> getExecutions(final String wid) {
        return this.store.entrySet().stream().filter(e -> e.getKey().startsWith(wid + "/")).
                map(e -> e.getValue()).sorted((e1, e2) -> e2.getInitiatedAt().compareTo(e1.getInitiatedAt())).
                collect(Collectors.toList());
    }

    private String getStoreId(final String wid, final String execId) {
        return wid + "/" + execId;
    }
}
