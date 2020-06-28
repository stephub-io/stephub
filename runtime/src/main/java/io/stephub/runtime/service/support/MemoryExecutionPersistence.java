package io.stephub.runtime.service.support;

import io.stephub.json.Json;
import io.stephub.provider.api.model.StepResponse;
import io.stephub.runtime.model.Execution;
import io.stephub.runtime.model.ExecutionInstruction;
import io.stephub.runtime.model.RuntimeSession;
import io.stephub.runtime.model.Workspace;
import io.stephub.runtime.service.ExecutionPersistence;
import io.stephub.runtime.service.exception.ExecutionException;
import lombok.extern.slf4j.Slf4j;
import net.jodah.expiringmap.ExpirationPolicy;
import net.jodah.expiringmap.ExpiringMap;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
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

    private ExpiringMap<String, Execution> store;

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
        final Execution execution = Execution.builder().
                instruction(instruction).
                id(UUID.randomUUID().toString()).
                sessionSettings(sessionSettings).
                initiatedAt(new Date()).
                backlog(instruction.buildItems(workspace)).
                build();
        this.store.put(this.getStoreId(workspace.getId(), execution.getId()), execution);
        return execution;
    }

    private Execution.ExecutionItem doLifecycleAndGetNext(final Execution execution) {
        synchronized (execution) {
            if (execution.getStatus() == INITIATED) {
                log.debug("Execution={} started", execution);
                execution.setStatus(EXECUTING);
                execution.setStartedAt(new Date());
            }
            final Optional<Execution.ExecutionItem> next = execution.getBacklog().stream().
                    filter((executionItem -> executionItem.getStatus() == INITIATED)).
                    findFirst();
            if (next.isPresent()) {
                log.debug("Next item={} in execution={}", next.get(), execution);
                return next.get();
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
        final Execution execution = this.getSafeExecution(wid, execId);
        Execution.ExecutionItem executionItem;
        while ((executionItem = this.doLifecycleAndGetNext(execution)) != null) {
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
        }
    }

    @Override
    public Execution getExecution(final String wid, final String execId) {
        return this.getSafeExecution(wid, execId);
    }

    @NotNull
    private Execution getSafeExecution(final String wid, final String execId) {
        final Execution execution = this.store.get(this.getStoreId(wid, execId));
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
