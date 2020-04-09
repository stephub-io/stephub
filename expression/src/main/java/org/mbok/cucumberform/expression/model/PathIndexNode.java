package org.mbok.cucumberform.expression.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.mbok.cucumberform.expression.EvaluationContext;
import org.mbok.cucumberform.json.Json;

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
