package io.stephub.cli.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.stephub.cli.exception.CommandException;
import io.stephub.cli.model.ImportWorkspace;
import io.stephub.server.api.model.Identifiable;
import io.stephub.server.api.model.Variable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import static io.stephub.cli.config.ObjectMapperConfig.YAML;

@Service
@Slf4j
public class WorkspaceYamlReader {
    @Autowired
    @Qualifier(YAML)
    private ObjectMapper yamlMapper;

    public ImportWorkspace read(final File... files) throws IOException {
        for (final File file : files) {
            if (!file.isFile()) {
                throw new CommandException("File '" + file.getName() + "' is a directory");
            } else if (!file.exists()) {
                throw new CommandException("File '" + file.getName() + "' doesn't exist");
            }
        }
        final ImportWorkspace target = new ImportWorkspace();
        for (final File file : files) {
            final ImportWorkspace increment = this.yamlMapper.readValue(file, ImportWorkspace.class);
            this.importFeatureFiles(file, target, increment);
            this.merge(target, file.getName(), increment);
        }
        return target;
    }

    private void importFeatureFiles(final File from, final ImportWorkspace target, final ImportWorkspace increment) throws IOException {
        if (increment.getFeatureFiles() != null) {
            final Path workdir = new File("./").toPath().toAbsolutePath();
            final Path base = from.getParentFile().toPath();
            for (final String sf : increment.getFeatureFiles()) {
                log.debug("Resolving features files from {} for pattern {}", base, sf);
                final PathMatcher pathMatcher = FileSystems.getDefault()
                        .getPathMatcher("glob:" + sf);
                final List<Path> resolvedFeatureFiles = new ArrayList<>();
                Files.walkFileTree(base, new SimpleFileVisitor<>() {
                    @Override
                    public FileVisitResult visitFile(final Path path,
                                                     final BasicFileAttributes attrs) throws IOException {
                        if (pathMatcher.matches(path)
                                || path.startsWith(base) && pathMatcher.matches(base.relativize(path))) {
                            resolvedFeatureFiles.add(path);
                        }
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFileFailed(final Path file, final IOException exc)
                            throws IOException {
                        return FileVisitResult.CONTINUE;
                    }
                });
                if (resolvedFeatureFiles.isEmpty()) {
                    log.warn("No feature files resolved for '{}' defined in '{}'", sf, from.getPath());
                }
                for (final Path path : resolvedFeatureFiles) {
                    if (!target.getFeatureFiles().contains(path.toAbsolutePath().toString())) {
                        target.getFeatureFiles().add(workdir.relativize(path).toString());
                    }
                }
            }
        }
    }

    private void merge(final ImportWorkspace target, final String file, final ImportWorkspace increment) {
        if (increment.getName() != null) {
            target.setName(increment.getName());
        }
        if (increment.getGherkinPreferences() != null) {
            if (increment.getGherkinPreferences().getAssignmentKeywords() != null) {
                this.mergePrimitiveList(target.getGherkinPreferences().getAssignmentKeywords(),
                        increment.getGherkinPreferences().getAssignmentKeywords());
            }
            if (increment.getGherkinPreferences().getStepKeywords() != null) {
                this.mergePrimitiveList(target.getGherkinPreferences().getStepKeywords(), increment.getGherkinPreferences().getStepKeywords());
            }
        }
        if (increment.getProviders() != null) {
            this.mergeList(target.getProviders(), increment.getProviders(), p -> {
                throw new MergeException("File '" + file + "' contains a duplicate provider '" + p + "'");
            });
        }
        if (increment.getStepDefinitions() != null) {
            this.mergeList(target.getStepDefinitions(), increment.getStepDefinitions(), s -> {
                throw new MergeException("File '" + file + "' contains a duplicate step definition for spec '" + s.getSpec() + "'");
            });
        }
        if (increment.getFeatures() != null) {
            this.mergeList(target.getFeatures(), increment.getFeatures(), f -> {
                throw new MergeException("File '" + file + "' contains a duplicate feature named '" + f.getName() + "'");
            });
        }
        if (increment.getVariables() != null) {
            this.mergeList(target.getVariables(), increment.getVariables());
        }
        if (increment.getBeforeFixtures() != null) {
            this.mergeList(target.getBeforeFixtures(), increment.getBeforeFixtures(), f -> {
                throw new MergeException("File '" + file + "' contains a duplicate before fixture '" + f.getName() + "'");
            });
        }
        if (increment.getAfterFixtures() != null) {
            this.mergeList(increment.getAfterFixtures(), increment.getAfterFixtures(), f -> {
                throw new MergeException("File '" + file + "' contains a duplicate after fixture '" + f.getName() + "'");
            });
        }
    }

    private void mergeList(final Map<String, Variable> target, final Map<String, Variable> source) {
        target.putAll(source);
    }

    private <T extends Identifiable> void mergeList(final List<T> target, final List<T> source, final Consumer<T> dupplicateConsumer) {
        source.stream().filter(i ->
        {
            target.stream().filter(a -> this.isSame(i, a)).findFirst().ifPresent(dupplicateConsumer::accept);
            return true;
        }).forEach(target::add);
    }

    private <T> void mergePrimitiveList(final List<T> target, final List<T> source) {
        source.stream().filter(i -> !target.contains(i)).
                forEach(target::add);
    }

    public static class MergeException extends RuntimeException {
        public MergeException(final String message) {
            super(message);
        }
    }

    private boolean isSame(final Identifiable a, final Identifiable b) {
        return Objects.equals(a.getId(), b.getId());
    }
}
