package io.stephub.expression.model;

import io.stephub.expression.EvaluationContext;
import io.stephub.json.JsonNumber;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@EqualsAndHashCode(callSuper = false)
@ToString
public class JsonNumberNode extends JsonValueNode<JsonNumber> {
    private Number value;

    @Override
    public JsonNumber evaluate(EvaluationContext ec) {
        return new JsonNumber(value);
    }
}
