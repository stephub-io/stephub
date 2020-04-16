package io.stephub.expression.model;

import io.stephub.expression.EvaluationContext;
import io.stephub.json.Json;

public abstract class Node<J extends Json> {
    public abstract J evaluate(EvaluationContext ec);
}
