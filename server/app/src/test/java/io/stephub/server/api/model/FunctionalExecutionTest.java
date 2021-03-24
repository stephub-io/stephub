package io.stephub.server.api.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static io.stephub.server.api.model.Execution.ExecutionStatus.COMPLETED;
import static org.hamcrest.MatcherAssert.assertThat;

@Slf4j
class FunctionalExecutionTest {

    @Test
    public void testFeatureStatusAggregation() throws JsonProcessingException {
        final FunctionalExecution.FeatureExecutionItem feature = FunctionalExecution.FeatureExecutionItem.builder().name("F1").scenarios(
                Collections.singletonList(FunctionalExecution.ScenarioExecutionItem.builder().name("S1").
                        step(FunctionalExecution.StepExecutionItem.builder().step("Hello").status(COMPLETED).build()).build())
        ).build();
        assertThat(feature.getStatus(), CoreMatchers.equalTo(COMPLETED));
    }
}