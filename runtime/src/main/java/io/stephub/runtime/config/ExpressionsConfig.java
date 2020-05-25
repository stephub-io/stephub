package io.stephub.runtime.config;

import io.stephub.expression.ExpressionEvaluator;
import io.stephub.expression.impl.DefaultExpressionEvaluator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ExpressionsConfig {
    @Bean
    public ExpressionEvaluator expressionEvaluator() {
        return new DefaultExpressionEvaluator();
    }
}
