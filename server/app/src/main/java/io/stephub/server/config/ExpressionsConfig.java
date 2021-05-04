package io.stephub.server.config;

import io.stephub.expression.ExpressionEvaluator;
import io.stephub.expression.FunctionFactory;
import io.stephub.expression.impl.DefaultExpressionEvaluator;
import io.stephub.json.JsonInteger;
import io.stephub.json.JsonNumber;
import io.stephub.json.JsonString;
import org.apache.commons.lang3.Validate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;

@Configuration
public class ExpressionsConfig {
    @Bean
    public ExpressionEvaluator expressionEvaluator() {
        return new DefaultExpressionEvaluator();
    }

    @Bean
    public FunctionFactory functionFactory() {
        return new FunctionFactory() {
            @Override
            public Function createFunction(final String name) {
                switch (name) {
                    case "random_uuid":
                        return (args) -> new JsonString(UUID.randomUUID().toString());
                    case "random_integer":
                        return (args) -> {
                            Validate.isTrue(args.length == 2, "Expected min and max parameters");
                            Validate.isTrue(args[0] instanceof JsonNumber);
                            Validate.isTrue(args[1] instanceof JsonNumber);
                            final int min = ((JsonNumber) args[0]).getValue().intValue();
                            final int max = ((JsonNumber) args[1]).getValue().intValue();
                            return new JsonInteger(min + (int) Math.round(Math.random() * (max - min)));
                        };
                }
                return null;
            }
        };
    }
}
