package io.stephub.runtime.service.support;

import io.stephub.runtime.model.Execution;
import io.stephub.runtime.model.ExecutionInstruction;
import io.stephub.runtime.service.ExecutionPersistence;
import io.stephub.runtime.service.exception.ExecutionException;
import net.jodah.expiringmap.ExpirationPolicy;
import net.jodah.expiringmap.ExpiringMap;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
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
    public Execution initExecution(final String wid, final ExecutionInstruction instruction) {
        final Execution execution = Execution.builder().
                instruction(instruction).
                id(UUID.randomUUID().toString()).
                initiatedAt(new Date()).
                build();
        this.store.put(this.getStoreId(wid, execution.getId()), execution);
        return execution;
    }

    @Override
    public void doWithinExecution(final String wid, final String execId, final WithinExecutionCommand command) {
        final Execution execution = this.getSafeExecution(wid, execId);
        execution.setStartedAt(new Date());
        execution.setStatus(Execution.ExecutionStatus.EXECUTING);
        try {
            command.execute(execution.getInstruction(), new ResultCollector() {
                @Override
                public void collect(final Execution.ExecutionResult result) {
                    execution.getResults().add(result);
                }
            });
        } finally {
            execution.setStatus(Execution.ExecutionStatus.COMPLETED);
            execution.setCompletedAt(new Date());
            synchronized (execution) {
                execution.notifyAll();
            }
        }
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

    private String getStoreId(final String wid, final String execId) {
        return wid + "-" + execId;
    }
}
