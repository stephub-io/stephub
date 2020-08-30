package io.stephub.cli.config.picocli;

import io.stephub.expression.ExpressionEvaluator;
import io.stephub.expression.ParseException;
import io.stephub.expression.impl.DefaultExpressionEvaluator;
import io.stephub.expression.impl.SimpleEvaluationContext;
import io.stephub.json.Json;
import io.stephub.json.JsonException;
import org.springframework.expression.ExpressionException;
import org.springframework.stereotype.Component;
import picocli.CommandLine;

@Component
public class JsonConverter implements CommandLine.ITypeConverter<Json> {
    private final ExpressionEvaluator evaluator = new DefaultExpressionEvaluator();

    @Override
    public Json convert(final String s) throws Exception {
        try {
            // Try to evaluate as native JSON value
            return this.evaluator.evaluate(s, SimpleEvaluationContext.builder().build());
        } catch (final ExpressionException | ParseException | JsonException e) {
            return this.evaluator.evaluate(
                    "\"" +
                            s.replaceAll("\"", "\\\"") +
                            "\"",
                    SimpleEvaluationContext.builder().build());
        }

    }
}
