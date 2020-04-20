package io.stephub.runtime.service;

import io.stephub.expression.ParseException;
import io.stephub.provider.spec.PatternType;
import io.stephub.provider.spec.StepSpec;
import io.stephub.runtime.service.GherkinPatternMatcher.StepMatch;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;


@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {GherkinPatternMatcher.class})
@Slf4j
public class GherkinPatternMatcherTest {
    @Autowired
    private GherkinPatternMatcher patternMatcher;

    @Test
    public void testDocString() {
        final StepSpec stepSpec = StepSpec.builder().pattern("Do with DocString payload").
                patternType(PatternType.REGEX).
                payload(StepSpec.PayloadType.DOC_STRING).
                build();
        final StepMatch match = this.patternMatcher.matches(stepSpec,
                "Do with DocString payload\n" +
                        "  \"\"\"\n" +
                        "  My doc string line 1\n" +
                        "  My doc string line 2\n" +
                        "  \"\"\"");
        assertThat(match, notNullValue());
        assertThat(match.getDocString(), equalTo(
                "My doc string line 1\n" +
                        "My doc string line 2"));
    }

    @Test
    public void testDocStringMissingOffset() {
        final StepSpec stepSpec = StepSpec.builder().pattern("Do with DocString payload").
                patternType(PatternType.REGEX).
                payload(StepSpec.PayloadType.DOC_STRING).
                build();
        final StepMatch match = this.patternMatcher.matches(stepSpec,
                "Do with DocString payload\n" +
                        "  \"\"\"\n" +
                        "My doc string line 1\n" +
                        "My doc string line 2\n" +
                        "\"\"\"");
        assertThat(match, notNullValue());
        assertThat(match.getDocString(), equalTo(
                "My doc string line 1\n" +
                        "My doc string line 2"));
    }

    @Test
    public void testDocMissingEndDelimiter() {
        final StepSpec stepSpec = StepSpec.builder().pattern("Do with DocString payload").
                patternType(PatternType.REGEX).
                payload(StepSpec.PayloadType.DOC_STRING).
                build();
        final ParseException e = assertThrows(ParseException.class, () -> {
            this.patternMatcher.matches(stepSpec,
                    "Do with DocString payload\n" +
                            "  \"\"\"\n" +
                            "My doc string line 1\n");
        });
        log.debug("Exception output", e);
    }

    @Test
    public void testMissingDocString() {
        final StepSpec stepSpec = StepSpec.builder().pattern("Do with DocString payload").
                patternType(PatternType.REGEX).
                payload(StepSpec.PayloadType.DOC_STRING).
                build();
        final ParseException e = assertThrows(ParseException.class, () -> {
            this.patternMatcher.matches(stepSpec,
                    "Do with DocString payload");
        });
        log.debug("Exception output", e);
    }

    @Test
    public void testMissingDocStringStartDelimiter() {
        final StepSpec stepSpec = StepSpec.builder().pattern("Do with DocString payload").
                patternType(PatternType.REGEX).
                payload(StepSpec.PayloadType.DOC_STRING).
                build();
        final ParseException e = assertThrows(ParseException.class, () -> {
            this.patternMatcher.matches(stepSpec,
                    "Do with DocString payload\n-Invalid delimiter");
        });
        log.debug("Exception output", e);
    }
}