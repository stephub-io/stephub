package io.stephub.expression.model;

import io.stephub.expression.EvaluationContext;
import io.stephub.json.Json;

public interface AssignableNode {
    void assign(EvaluationContext evaluationContext, Json value);
}
