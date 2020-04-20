package io.stephub.expression.model;

import io.stephub.expression.EvaluationContext;
import io.stephub.json.Json;
import lombok.*;

@AllArgsConstructor
@Builder
@Getter
@EqualsAndHashCode(callSuper = false)
@ToString
public class ExprNode extends Node<Json> {
    private final JsonValueNode<? extends Json> json;

    @Override
    public Json evaluate(final EvaluationContext ec) {
        return json.evaluate(ec);
    }
}
