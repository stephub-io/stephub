package io.stephub.runtime.service;

import io.stephub.json.Json;
import io.stephub.provider.api.model.StepResponse;
import io.stephub.runtime.model.Context;
import io.stephub.runtime.model.RuntimeSession;
import io.stephub.runtime.model.StepInstruction;

import java.util.List;

public interface SessionService {
    List<RuntimeSession> getSessions(Context ctx, String wid);

    RuntimeSession startSession(Context ctx, String wid);

    void stopSession(Context ctx, String wid, String sid);

    RuntimeSession getSession(Context ctx, String wid, String sid);

    StepResponse<Json> execute(Context ctx, String wid, String sid, StepInstruction stepInstruction);
}
