package org.mbok.cucumberform.runtime.service;

import org.mbok.cucumberform.provider.StepResponse;
import org.mbok.cucumberform.runtime.model.Context;
import org.mbok.cucumberform.runtime.model.RuntimeSession;
import org.mbok.cucumberform.runtime.model.StepExecution;
import org.mbok.cucumberform.runtime.model.Workspace;

import java.util.List;

public interface SessionService {
    List<RuntimeSession> getSessions(Context ctx, String wid);

    RuntimeSession startSession(Context ctx, Workspace workspace);

    void stopSession(Context ctx, String wid, String sid);

    RuntimeSession getSession(Context ctx, String wid, String sid);

    StepResponse execute(Context ctx, String wid, String sid, StepExecution stepExecution);
}
