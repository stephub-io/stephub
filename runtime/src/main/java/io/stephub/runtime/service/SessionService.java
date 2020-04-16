package io.stephub.runtime.service;

import io.stephub.provider.StepResponse;
import io.stephub.runtime.model.Context;
import io.stephub.runtime.model.RuntimeSession;
import io.stephub.runtime.model.StepExecution;
import io.stephub.runtime.model.Workspace;

import java.util.List;

public interface SessionService {
    List<RuntimeSession> getSessions(Context ctx, String wid);

    RuntimeSession startSession(Context ctx, Workspace workspace);

    void stopSession(Context ctx, String wid, String sid);

    RuntimeSession getSession(Context ctx, String wid, String sid);

    StepResponse execute(Context ctx, String wid, String sid, StepExecution stepExecution);
}
