package io.stephub.cli.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.stephub.cli.exception.CommandException;
import io.stephub.server.api.model.Variable;
import io.stephub.server.api.model.Workspace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static io.stephub.cli.config.ObjectMapperConfig.YAML;

@Service
public class WorkspaceYamlReader {
    @Autowired
    @Qualifier(YAML)
    private ObjectMapper yamlMapper;

    public Workspace read(final Resource... files) throws IOException {
        for (final Resource file : files) {
            if (!file.isFile()) {
                throw new CommandException("File '" + file.getFilename() + "' is a directory");
            } else if (!file.exists()) {
                throw new CommandException("File '" + file.getFilename() + "' doesn't exist");
            } else if (!file.isReadable()) {
                throw new CommandException("File '" + file.getFilename() + "' isn't readable");
            }
        }
        final Workspace target = new Workspace();
        for (final Resource file : files) {
            final Workspace increment = this.yamlMapper.readValue(file.getInputStream(), Workspace.class);
            this.merge(target, increment);
        }
        return target;
    }

    private void merge(final Workspace target, final Workspace increment) {
        if (increment.getName() != null) {
            target.setName(increment.getName());
        }
        if (increment.getGherkinPreferences() != null) {
            if (increment.getGherkinPreferences().getStepOutputAssignmentSuffixes() != null) {
                this.mergeList(target.getGherkinPreferences().getStepOutputAssignmentSuffixes(),
                        increment.getGherkinPreferences().getStepOutputAssignmentSuffixes());
            }
            if (increment.getGherkinPreferences().getStepPrefixes() != null) {
                this.mergeList(target.getGherkinPreferences().getStepPrefixes(), increment.getGherkinPreferences().getStepPrefixes());
            }
        }
        if (increment.getProviders() != null) {
            this.mergeList(target.getProviders(), increment.getProviders());
        }
        if (increment.getStepDefinitions() != null) {
            this.mergeList(target.getStepDefinitions(), increment.getStepDefinitions());
        }
        if (increment.getFeatures() != null) {
            this.mergeList(target.getFeatures(), increment.getFeatures());
        }
        if (increment.getVariables() != null) {
            this.mergeList(target.getVariables(), increment.getVariables());
        }
        if (increment.getBeforeFixtures() != null) {
            this.mergeList(target.getBeforeFixtures(), increment.getBeforeFixtures());
        }
        if (increment.getAfterFixtures() != null) {
            this.mergeList(increment.getAfterFixtures(), increment.getAfterFixtures());
        }
    }

    private void mergeList(final Map<String, Variable> target, final Map<String, Variable> source) {
        target.putAll(source);
    }

    private <T> void mergeList(final List<T> target, final List<T> source) {
        source.stream().filter(i -> !target.contains(i)).
                forEach(target::add);
    }
}
