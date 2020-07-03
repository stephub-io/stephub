package io.stephub.runtime.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static io.stephub.runtime.model.Execution.ExecutionStatus.COMPLETED;
import static org.hamcrest.MatcherAssert.assertThat;

@Slf4j
class ExecutionTest {

    @Test
    public void testFeatureStatusAggregation() throws JsonProcessingException {
        final Execution.FeatureExecutionItem feature = Execution.FeatureExecutionItem.builder().name("F1").scenarios(
                Collections.singletonList(Execution.ScenarioExecutionItem.builder().name("S1").
                        step(Execution.StepExecutionItem.builder().instruction("Hello").status(COMPLETED).build()).build())
        ).build();
        assertThat(feature.getStatus(), CoreMatchers.equalTo(COMPLETED));
    }
}