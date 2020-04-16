package io.stephub.expression.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import io.stephub.expression.EvaluationContext;
import io.stephub.json.Json;

@AllArgsConstructor
@Getter
public class PathIndexNode extends JsonValueNode<Json> {
    private final String valueText;
    private final JsonValueNode<?> valueNode;

    @Override
    public Json evaluate(final EvaluationContext ec) {
        return valueNode.evaluate(ec);
    }
}
