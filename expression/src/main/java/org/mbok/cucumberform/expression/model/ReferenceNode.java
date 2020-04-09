package org.mbok.cucumberform.expression.model;

import lombok.AllArgsConstructor;
import org.mbok.cucumberform.expression.EvaluationContext;
import org.mbok.cucumberform.json.Json;

@AllArgsConstructor
public class ReferenceNode extends JsonValueNode<Json> {
    private PathNode pathNode;

    @Override
    public Json evaluate(EvaluationContext ec) {
        return pathNode.evaluate(ec);
    }
}
