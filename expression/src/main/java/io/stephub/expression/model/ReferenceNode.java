package io.stephub.expression.model;

import lombok.AllArgsConstructor;
import io.stephub.expression.EvaluationContext;
import io.stephub.json.Json;

@AllArgsConstructor
public class ReferenceNode extends JsonValueNode<Json> {
    private PathNode pathNode;

    @Override
    public Json evaluate(EvaluationContext ec) {
        return pathNode.evaluate(ec);
    }
}
