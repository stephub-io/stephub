package io.stephub.server.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.stephub.json.JsonObject;
import io.stephub.provider.api.model.ProviderOptions;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.URL;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import java.util.Arrays;
import java.util.regex.Matcher;

@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@SuperBuilder
public class ProviderSpec extends ProviderOptions<JsonObject> implements Identifiable {
    private static final String VERSION_PATTERN = "\\s*(>|>=|=|^)\\s*(\\d+(\\.\\d+(\\.\\d+)?)?).*?";

    @NotEmpty
    private String name;

    @Pattern(regexp = VERSION_PATTERN)
    private String version;

    @Valid
    private RemoteProviderConfig remoteConfig;

    @Override
    public JsonObject getOptions() {
        final JsonObject opt = super.getOptions();
        if (opt == null) {
            return new JsonObject();
        }
        return opt;
    }

    @Override
    @JsonIgnore
    public String getId() {
        return this.name;
    }

    public boolean matchesVersion(final String given) {
        if (StringUtils.isNotBlank(this.version) && StringUtils.isNotBlank(given)) {
            return VersionComparator.build(this.version).matches(new Version(this.version), new Version(given));
        }
        return true;
    }


    private enum VersionComparator {
        EQ, GT, GET;

        private static VersionComparator build(final String version) {
            final Matcher matcher = java.util.regex.Pattern.compile(VERSION_PATTERN).matcher(version);
            if (matcher.matches()) {
                switch (matcher.group(1)) {
                    case ">":
                        return GT;
                    case ">=":
                        return GET;
                }
            }
            return EQ;
        }

        boolean matches(final Version expected, final Version given) {
            switch (this) {
                case EQ:
                    return expected.equals(given);
                case GT:
                    return given.compareTo(expected) > 0;
                case GET:
                    return given.compareTo(expected) > 0 || expected.equals(given);
            }
            return false;
        }
    }

    private static class Version implements Comparable<Version> {
        private final int[] v = {0, 0, 0};

        private Version(final String str) {
            final Matcher matcher = java.util.regex.Pattern.compile(VERSION_PATTERN).matcher(str);
            if (matcher.matches()) {
                final String[] sps =
                        matcher.group(2).split("\\.");
                for (int i = 0; i < sps.length; i++) {
                    this.v[i] = Integer.parseInt(sps[i]);
                }
            }
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || this.getClass() != o.getClass()) {
                return false;
            }
            final Version version = (Version) o;
            return Arrays.equals(this.v, version.v);
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(this.v);
        }

        @Override
        public int compareTo(final Version other) {
            for (int i = 0; i < 3; i++) {
                if (this.v[i] != other.v[i]) {
                    return this.v[i] - other.v[i];
                }
            }
            return 0;
        }
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    @EqualsAndHashCode
    @ToString
    public static class RemoteProviderConfig {
        @URL
        private String url;
    }
}
