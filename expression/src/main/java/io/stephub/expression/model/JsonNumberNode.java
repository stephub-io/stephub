package io.stephub.expression.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import io.stephub.expression.EvaluationContext;
import io.stephub.json.JsonNumber;

@AllArgsConstructor
@Getter
public class JsonNumberNode extends JsonValueNode<JsonNumber> {
    private Number value;

    @Override
    public JsonNumber evaluate(EvaluationContext ec) {
        return new JsonNumber(value);
    }
}
