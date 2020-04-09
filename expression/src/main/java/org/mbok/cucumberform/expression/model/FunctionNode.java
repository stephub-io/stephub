package org.mbok.cucumberform.expression.model;

import lombok.AllArgsConstructor;
import org.mbok.cucumberform.expression.EvaluationContext;
import org.mbok.cucumberform.json.Json;
import org.mbok.cucumberform.json.JsonNull;

import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
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
