package io.stephub.cli.command;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.stephub.cli.client.WorkspaceClient;
import io.stephub.cli.exception.CommandException;
import io.stephub.cli.model.ImportWorkspace;
import io.stephub.cli.service.WorkspaceYamlReader;
import io.stephub.server.api.model.Workspace;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;

import static io.stephub.cli.config.ObjectMapperConfig.YAML;

@Component
@CommandLine.Command(name = "import", mixinStandardHelpOptions = true,
        exitCodeOnExecutionException = 34)
@Slf4j
public class ImportCommand extends BaseCommand implements Runnable {

    @Autowired
    private WorkspaceYamlReader workspaceYamlReader;

    @Autowired
    @Qualifier(YAML)
    private ObjectMapper yamlMapper;

    @Autowired
    private WorkspaceClient workspaceClient;

    @CommandLine.Option(names = {"-f"},
            description = "one ore more YAML workspace definition files to import",
            arity = "1..*", required = true,
            paramLabel = "file")
    private File[] files;

    @CommandLine.Option(names = {"--dry-run"}, description = "Dry-run to preview import of workspace", defaultValue = "false")
    private boolean dryRun;

    @Override
    public void run() {
        try {
            final ImportWorkspace toImport = this.workspaceYamlReader.read(this.files);
            log.info("Going to import workspace:\n{}", this.yamlMapper.
                    writerWithView(ImportWorkspace.InfoView.class)
                    .with(new DefaultPrettyPrinter())
                    .writeValueAsString(toImport));
            String id = toImport.getId();
            if (StringUtils.isBlank(id)) {
                id = toImport.getName();
            }
            final Workspace existing;
            if (StringUtils.isNoneBlank(toImport.getId())) {
                existing = this.workspaceClient.findWorkspace(this.getServerContext(), toImport.getId(), false);
                if (existing == null) {
                    log.info("No existing workspace found with id='{}', importing as a new one", toImport.getId());
                }
            } else if (StringUtils.isNoneBlank(toImport.getName())) {
                existing = this.workspaceClient.findWorkspace(this.getServerContext(), toImport.getName(), false);
                if (existing == null) {
                    log.info("No existing workspace found with name='{}', importing as a new one", toImport.getName());
                }
            } else {
                throw new CommandException("Import not possible without workspace id or name, please specify at least one of these.");
            }
            final Workspace imported;
            if (existing == null) {
                if (this.dryRun) {
                    log.info("Stopped import due to dry run");
                    return;
                }
                imported = this.workspaceClient.createWorkspace(this.getServerContext(), toImport);
            } else {
                if (this.dryRun) {
                    log.info("Stopped import due to dry run");
                    return;
                }
                toImport.setId(existing.getId());
                imported = this.workspaceClient.replaceWorkspace(this.getServerContext(), toImport);
            }
            log.info("Imported workspace successfully");
            if (imported.getErrors() != null && !imported.getErrors().isEmpty()) {
                log.warn("Note that imported workspace has errors:\n{}",
                        this.yamlMapper.
                                writerWithView(ImportWorkspace.InfoView.class)
                                .with(new DefaultPrettyPrinter())
                                .writeValueAsString(imported.getErrors()));
            }

        } catch (final IOException e) {
            throw new CommandException("Failed to import workspace: " + e.getMessage());
        }
    }
}
