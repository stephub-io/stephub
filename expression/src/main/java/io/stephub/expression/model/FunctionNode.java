package io.stephub.expression.model;

import io.stephub.expression.EvaluationContext;
import io.stephub.json.Json;
import io.stephub.json.JsonNull;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@ToString
public class FunctionNode extends JsonValueNode<Json> {
    private final String name;
    private final List<JsonValueNode<? extends Json>> arguments;

    @Override
    public Json evaluate(final EvaluationContext ec) {
        final List<? extends Json> ea = this.arguments.stream().
                map(a -> a.evaluate(ec)).collect(Collectors.toList());
        final Json result = ec.createFunction(this.name).invoke(ea.toArray(new Json[ea.size()]));
        if (result != null) {
            return result;
        } else {
            return new JsonNull();
        }
    }
}
