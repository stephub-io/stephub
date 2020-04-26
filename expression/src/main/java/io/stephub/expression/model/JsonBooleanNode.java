package io.stephub.expression.model;

import io.stephub.expression.EvaluationContext;
import io.stephub.json.JsonBoolean;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@ToString
public class JsonBooleanNode extends JsonValueNode<JsonBoolean> {
    private final boolean value;

    @Override
    public JsonBoolean evaluate(final EvaluationContext ec) {
        return JsonBoolean.valueOf(this.value);
    }
}
