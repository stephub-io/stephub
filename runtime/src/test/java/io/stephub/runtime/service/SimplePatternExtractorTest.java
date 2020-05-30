package io.stephub.runtime.service;

import io.stephub.runtime.model.GherkinPreferences;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {SimplePatternExtractor.class})
@Slf4j
class SimplePatternExtractorTest {

    @Autowired
    private SimplePatternExtractor extractor;

    @Test
    public void testFaultyArgument() {
        assertThat(
            extractor.extract(GherkinPreferences.builder().build(),"{abc", false).getRegexPattern().matcher("When {abc").matches(),
                equalTo(true));
    }
}