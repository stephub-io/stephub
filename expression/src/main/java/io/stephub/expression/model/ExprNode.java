package io.stephub.expression.model;

import lombok.Builder;
import lombok.Getter;
import io.stephub.expression.EvaluationContext;
import io.stephub.json.Json;

@Builder
@Getter
public class ExprNode extends Node {
    private final JsonValueNode json;

    @Override
    public Json evaluate(final EvaluationContext ec) {
        return json.evaluate(ec);
    }
}
