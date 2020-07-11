package io.stephub.server.service;

import io.stephub.expression.EvaluationContext;
import io.stephub.expression.impl.SimpleEvaluationContext;
import io.stephub.json.Json;
import io.stephub.json.schema.JsonSchema;
import io.stephub.provider.api.model.StepResponse;
import io.stephub.provider.api.model.spec.PatternType;
import io.stephub.provider.api.model.spec.StepSpec;
import io.stephub.server.api.SessionExecutionContext;
import io.stephub.server.api.StepExecution;
import io.stephub.server.api.model.NestedStepResponse;
import io.stephub.server.api.model.Workspace;
import io.stephub.server.api.model.customsteps.BasicStepDefinition;
import io.stephub.server.config.ExpressionsConfig;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Duration;

import static io.stephub.provider.api.model.StepResponse.StepStatus.ERRONEOUS;
import static io.stephub.provider.api.model.StepResponse.StepStatus.PASSED;
import static io.stephub.server.service.StepExecutionResolver.RECURSIVE_STEP_CALL_SEQUENCE_DETECTED;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {StepExecutionResolver.class, ExpressionsConfig.class,
        GherkinPatternMatcher.class, StepEvaluationDelegate.class, SimplePatternExtractor.class})
@Slf4j
class StepExecutionResolverTest {
    @MockBean
    ProvidersFacade providersFacade;

    @Autowired
    private StepExecutionResolver executionResolver;

    @Test
    public void testExecutionCustomStep() {
        // Given
        final Workspace workspace = Workspace.builder().
                stepDefinition(
                        BasicStepDefinition.builder().
                                spec(
                                        StepSpec.<JsonSchema>builder().
                                                pattern("Custom step").
                                                patternType(PatternType.SIMPLE)
                                                .build()
                                ).
                                step("When Inner provider step").
                                build()
                ).
                build();
        final StepExecution innerStepExec = mock(StepExecution.class);
        when(innerStepExec.execute(any(SessionExecutionContext.class), any(EvaluationContext.class))).
                thenReturn(
                        StepResponse.<Json>builder().status(PASSED).
                                duration(Duration.ofMinutes(3)).build()
                );
        when(this.providersFacade.resolveStepExecution(
                "When Inner provider step",
                workspace
        )).thenReturn(innerStepExec);

        // Call
        final StepExecution execution = this.executionResolver.resolveStepExecution(
                "When custom step",
                workspace
        );

        // Expect
        final NestedStepResponse response = (NestedStepResponse) execution.execute(mock(SessionExecutionContext.class),
                SimpleEvaluationContext.builder().build());
        assertThat(response.getStatus(), equalTo(PASSED));
        assertThat(response.getDuration(), equalTo(Duration.ofMinutes(3)));
    }

    @Test
    public void testExecutionMultipleCustomSteps() {
        // Given
        final Workspace workspace = Workspace.builder().
                stepDefinition(
                        BasicStepDefinition.builder().
                                spec(
                                        StepSpec.<JsonSchema>builder().
                                                pattern("Custom step 1").
                                                patternType(PatternType.SIMPLE)
                                                .build()
                                ).
                                step("When inner provider step").
                                build()
                ).
                stepDefinition(
                        BasicStepDefinition.builder().
                                spec(
                                        StepSpec.<JsonSchema>builder().
                                                pattern("Custom step 2").
                                                patternType(PatternType.SIMPLE)
                                                .build()
                                ).
                                step("When custom step 1").
                                step("When inner provider step").
                                build()
                ).
                build();
        final StepExecution innerStepExec = mock(StepExecution.class);
        when(innerStepExec.execute(any(SessionExecutionContext.class), any(EvaluationContext.class))).
                thenReturn(
                        StepResponse.<Json>builder().status(PASSED).
                                duration(Duration.ofMinutes(3)).build()
                );
        when(this.providersFacade.resolveStepExecution(
                "When inner provider step",
                workspace
        )).thenReturn(innerStepExec);

        // Call
        final StepExecution execution = this.executionResolver.resolveStepExecution(
                "When custom step 2",
                workspace
        );

        // Expect
        final NestedStepResponse response = (NestedStepResponse) execution.execute(mock(SessionExecutionContext.class),
                SimpleEvaluationContext.builder().build());
        assertThat(response.getStatus(), equalTo(PASSED));
        assertThat(response.getDuration(), equalTo(Duration.ofMinutes(6)));
        assertThat(response.getSubResponses().size(), equalTo(1));
        assertThat(response.getSubResponses().get(0).getEntries().get(0).getInstruction(), equalTo("When custom step 1"));
        assertThat(response.getSubResponses().get(0).getEntries().get(0).getResponse(), instanceOf(NestedStepResponse.class));
        assertThat(response.getSubResponses().get(0).getEntries().get(1).getInstruction(), equalTo("When inner provider step"));
        assertThat(response.getSubResponses().get(0).getEntries().get(1).getResponse(), instanceOf(StepResponse.class));
    }

    @Test
    public void testRecursiveSteps() {
        // Given
        final Workspace workspace = Workspace.builder().
                stepDefinition(
                        BasicStepDefinition.builder().
                                spec(
                                        StepSpec.<JsonSchema>builder().
                                                pattern("Custom step 1").
                                                patternType(PatternType.SIMPLE)
                                                .build()
                                ).
                                step("When custom step 2").
                                build()
                ).
                stepDefinition(
                        BasicStepDefinition.builder().
                                spec(
                                        StepSpec.<JsonSchema>builder().
                                                pattern("Custom step 2").
                                                patternType(PatternType.SIMPLE)
                                                .build()
                                ).
                                step("When custom step 1").
                                step("When inner provider step").
                                build()
                ).
                build();
        final StepExecution innerStepExec = mock(StepExecution.class);
        when(innerStepExec.execute(any(SessionExecutionContext.class), any(EvaluationContext.class))).
                thenReturn(
                        StepResponse.<Json>builder().status(PASSED).
                                duration(Duration.ofMinutes(3)).build()
                );
        when(this.providersFacade.resolveStepExecution(
                "When inner provider step",
                workspace
        )).thenReturn(innerStepExec);

        // Call
        final StepExecution execution = this.executionResolver.resolveStepExecution(
                "When custom step 2",
                workspace
        );

        // Expect
        final NestedStepResponse response = (NestedStepResponse) execution.execute(mock(SessionExecutionContext.class),
                SimpleEvaluationContext.builder().build());
        assertThat(response.getStatus(), equalTo(ERRONEOUS));
        assertThat(response.getSubResponses().size(), equalTo(1));
        assertThat(response.getSubResponses().get(0).getEntries().size(), equalTo(1));
        assertThat(response.getSubResponses().get(0).getEntries().get(0).getInstruction(), equalTo("When custom step 1"));
        assertThat(((NestedStepResponse) response.getSubResponses().get(0).getEntries().get(0).getResponse()).
                getSubResponses().size(), equalTo(1));
        assertThat(((NestedStepResponse) response.getSubResponses().get(0).getEntries().get(0).getResponse()).
                getSubResponses().get(0).getEntries().size(), equalTo(1));
        assertThat(((NestedStepResponse) response.getSubResponses().get(0).getEntries().get(0).getResponse()).
                getSubResponses().get(0).getEntries().get(0).getInstruction(), equalTo("When custom step 2"));
        assertThat(((NestedStepResponse) response.getSubResponses().get(0).getEntries().get(0).getResponse()).
                getSubResponses().get(0).getEntries().get(0).getResponse().getErrorMessage(), equalTo(RECURSIVE_STEP_CALL_SEQUENCE_DETECTED));
    }
}