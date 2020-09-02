package io.stephub.expression.model;

import io.stephub.expression.EvaluationContext;
import io.stephub.json.Json;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@ToString
public class ReferenceNode extends JsonValueNode<Json> implements AssignableNode {
    private final PathNode pathNode;

    @Override
    public Json evaluate(final EvaluationContext ec) {
        return this.pathNode.evaluate(ec);
    }

    @Override
    public void assign(final EvaluationContext ec, final Json value) {
        this.pathNode.assign(ec, value);
    }
}
