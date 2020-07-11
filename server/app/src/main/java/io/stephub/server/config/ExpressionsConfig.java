package io.stephub.server.config;

import io.stephub.expression.ExpressionEvaluator;
import io.stephub.expression.FunctionFactory;
import io.stephub.expression.impl.DefaultExpressionEvaluator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
            public Function createFunction(String name) {
                return null;
            }
        };
    }
}
