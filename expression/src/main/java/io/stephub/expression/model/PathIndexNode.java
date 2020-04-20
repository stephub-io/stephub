package io.stephub.expression.model;

import io.stephub.expression.EvaluationContext;
import io.stephub.json.Json;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@EqualsAndHashCode(callSuper = false)
@ToString
public class PathIndexNode extends JsonValueNode<Json> {
    private final String valueText;
    private final JsonValueNode<?> valueNode;

    @Override
    public Json evaluate(final EvaluationContext ec) {
        return valueNode.evaluate(ec);
    }
}
