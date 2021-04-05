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
import io.stephub.server.api.model.StepResponseContext;
import io.stephub.server.api.model.Workspace;
import io.stephub.server.api.model.customsteps.BasicStepDefinition;
import io.stephub.server.config.ExpressionsConfig;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Duration;
import java.util.Optional;

import static io.stephub.provider.api.model.StepResponse.StepStatus.PASSED;
import static io.stephub.server.service.StepExecutionResolver.RECURSIVE_STEP_CALL_SEQUENCE_DETECTED;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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
        doAnswer((Answer<Object>) invocationOnMock -> {
            ((StepResponseContext) invocationOnMock.getArgument(2)).completeStep(
                    StepResponse.<Json>builder().status(PASSED).
                            duration(Duration.ofMinutes(3)).build());
            return null;
        }).when(innerStepExec).execute(any(SessionExecutionContext.class), any(EvaluationContext.class),
                any(StepResponseContext.class));

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
        final StepResponseContext responseContext = mock(StepResponseContext.class, Mockito.RETURNS_DEEP_STUBS);
        execution.execute(mock(SessionExecutionContext.class),
                SimpleEvaluationContext.builder().build(), responseContext);
        verify(responseContext.nested().group(Optional.empty())).startStep("When Inner provider step");
        verify(responseContext.nested().group(Optional.empty()).startStep("When Inner provider step")).completeStep(
                StepResponse.<Json>builder().status(PASSED).
                        duration(Duration.ofMinutes(3)).build()
        );
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
        doAnswer((Answer<Object>) invocationOnMock -> {
            ((StepResponseContext) invocationOnMock.getArgument(2)).completeStep(
                    StepResponse.<Json>builder().status(PASSED).
                            duration(Duration.ofMinutes(3)).build());
            return null;
        }).when(innerStepExec).execute(any(SessionExecutionContext.class), any(EvaluationContext.class),
                any(StepResponseContext.class));

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
        final StepResponseContext responseContext = mock(StepResponseContext.class, Mockito.RETURNS_DEEP_STUBS);
        execution.execute(mock(SessionExecutionContext.class),
                SimpleEvaluationContext.builder().build(), responseContext);

        verify(responseContext.nested().group(Optional.empty()), times(1)).startStep("When custom step 1");
        verify(responseContext.nested().group(Optional.empty()).startStep("When custom step 1"), times(1)).nested();
        verify(responseContext.nested().group(Optional.empty()).startStep("When custom step 1").nested().group(Optional.empty()), times(1)).startStep("When inner provider step");
        verify(responseContext.nested().group(Optional.empty()).startStep("When custom step 1").nested().group(Optional.empty()).startStep("When inner provider step"), times(1))
                .completeStep(
                        StepResponse.<Json>builder().status(PASSED).
                                duration(Duration.ofMinutes(3)).build()
                );
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
        doAnswer((Answer<Object>) invocationOnMock -> {
            ((StepResponseContext) invocationOnMock.getArgument(2)).completeStep(
                    StepResponse.<Json>builder().status(PASSED).
                            duration(Duration.ofMinutes(3)).build());
            return null;
        }).when(innerStepExec).execute(any(SessionExecutionContext.class), any(EvaluationContext.class),
                any(StepResponseContext.class));

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
        final StepResponseContext responseContext = mock(StepResponseContext.class, Mockito.RETURNS_DEEP_STUBS);
        execution.execute(mock(SessionExecutionContext.class),
                SimpleEvaluationContext.builder().build(), responseContext);

        verify(responseContext.nested().group(Optional.empty()).startStep("When custom step 1").
                nested().group(Optional.empty()).startStep("When custom step 2")).
                completeStep(argThat(response -> response.getErrorMessage().equals(RECURSIVE_STEP_CALL_SEQUENCE_DETECTED)));
    }
}