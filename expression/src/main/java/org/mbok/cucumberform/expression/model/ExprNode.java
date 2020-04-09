package org.mbok.cucumberform.expression.model;

import lombok.Builder;
import lombok.Getter;
import org.mbok.cucumberform.expression.EvaluationContext;
import org.mbok.cucumberform.json.Json;

@Builder
@Getter
public class ExprNode extends Node {
    private final JsonValueNode json;

    @Override
    public Json evaluate(final EvaluationContext ec) {
        return json.evaluate(ec);
    }
}
