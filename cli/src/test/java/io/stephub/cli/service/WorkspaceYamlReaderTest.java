package io.stephub.cli.service;

import au.com.origin.snapshots.SnapshotMatcher;
import au.com.origin.snapshots.junit5.SnapshotExtension;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.stephub.cli.config.ObjectMapperConfig;
import io.stephub.server.api.model.Workspace;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;

@ExtendWith({SpringExtension.class, SnapshotExtension.class})
@ContextConfiguration(classes = {WorkspaceYamlReader.class, ObjectMapperConfig.class})
@Slf4j
class WorkspaceYamlReaderTest {
    @Autowired
    private WorkspaceYamlReader reader;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testSimple() throws IOException {
        final Workspace workspace = this.reader.read(new ClassPathResource("workspace-simple.yml").getFile());
        SnapshotMatcher.expect(this.objectMapper.writeValueAsString(workspace)).toMatchSnapshot();
    }

    @Test
    public void testMerged() throws IOException {
        final Workspace workspace = this.reader.read(new ClassPathResource("workspace-simple.yml").getFile(), new ClassPathResource("workspace-inc-steps.yml").getFile());
        SnapshotMatcher.expect(this.objectMapper.writeValueAsString(workspace)).toMatchSnapshot();
    }

    @Test
    public void testFeaturesFromFile() throws IOException {
        final Workspace workspace = this.reader.read(new ClassPathResource("workspace-feature-file.yml").getFile());
        SnapshotMatcher.expect(this.objectMapper.writeValueAsString(workspace)).toMatchSnapshot();
    }
}