package io.stephub.server.api.model;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class ProviderSpecTest {
    @Test
    public void testVersionComparison() {
        assertThat(this.withVersion("0.1.2").matchesVersion("0.1.2"),
                is(true));
        assertThat(this.withVersion(">=0.1.2").matchesVersion("0.1.2"),
                is(true));
        assertThat(this.withVersion("0.1.2").matchesVersion("0.1"),
                is(false));
        assertThat(this.withVersion(">0.1.2").matchesVersion("0.1"),
                is(false));
        assertThat(this.withVersion(">=0.1.2").matchesVersion("0.1"),
                is(false));
        assertThat(this.withVersion(">0.1.2").matchesVersion("2"),
                is(true));
        assertThat(this.withVersion(">0.1.2").matchesVersion("0.1.3"),
                is(true));
        assertThat(this.withVersion(">0.1.2").matchesVersion("0.1.3-SNAPSHOT"),
                is(true));
    }

    private ProviderSpec withVersion(final String version) {
        return ProviderSpec.builder().version(version).build();
    }
}