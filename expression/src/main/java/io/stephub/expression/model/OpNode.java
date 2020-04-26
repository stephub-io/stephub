package io.stephub.expression.model;

import io.stephub.expression.EvaluationContext;
import io.stephub.json.Json;
import io.stephub.json.JsonString;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import static io.stephub.json.Json.JsonType.STRING;

@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@ToString
public class OpNode extends JsonValueNode<Json> {
    private interface OpFunction {
        Json apply(Json left, Json right);
    }

    public static enum Operator {
        PLUS(((left, right) -> {
            if (left instanceof JsonString || right instanceof JsonString) {
                return new JsonString(
                        ((JsonString) STRING.convertFrom(left)).getValue()
                                +
                                ((JsonString) STRING.convertFrom(right)).getValue());
            }
            throw new UnsupportedOperationException("Not implemented so far");
        }));

        Operator(final OpFunction func) {
            this.func = func;
        }

        private final OpFunction func;

        private Json apply(final Json left, final Json right) {
            return this.func.apply(left, right);
        }
    }

    private final Operator operator;
    private final JsonValueNode<? extends Json> left;
    private final JsonValueNode<? extends Json> right;

    @Override
    public Json evaluate(final EvaluationContext ec) {
        return this.operator.apply(this.left.evaluate(ec), this.right.evaluate(ec));
    }
}
