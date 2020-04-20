package io.stephub.expression.model;

import io.stephub.expression.EvaluationContext;
import io.stephub.json.Json;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@ToString
public class ReferenceNode extends JsonValueNode<Json> {
    private PathNode pathNode;

    @Override
    public Json evaluate(EvaluationContext ec) {
        return pathNode.evaluate(ec);
    }
}
