package org.jboss.windup.reporting.data.rules;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.phase.PreReportPfRenderingPhase;
import org.jboss.windup.config.projecttraversal.ProjectTraversalCache;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.model.resource.IgnoredFileModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.graph.service.WindupConfigurationService;
import org.jboss.windup.reporting.data.dto.ApplicationIgnoredFilesDto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RuleMetadata(
        phase = PreReportPfRenderingPhase.class,
        haltOnException = true
)
public class IgnoredFilesRuleProvider extends AbstractApiRuleProvider {

    public static final String PATH = "ignored-files";

    @Override
    public String getBasePath() {
        return "ignored-files";
    }

    @Override
    public Object getAll(GraphRewrite event) {
        GraphContext context = event.getGraphContext();
        WindupConfigurationModel configurationModel = WindupConfigurationService.getConfigurationModel(context);

        List<ApplicationIgnoredFilesDto> result = new ArrayList<>();

        for (FileModel inputPath : configurationModel.getInputPaths()) {
            ProjectModel application = inputPath.getProjectModel();
            GraphService<IgnoredFileModel> ignoredFilesModelService = new GraphService<>(context, IgnoredFileModel.class);

            List<ApplicationIgnoredFilesDto.IgnoredFileDto> filesDto = new ArrayList<>();
            for (IgnoredFileModel file : ignoredFilesModelService.findAll()) {
                Set<ProjectModel> fileApplications = ProjectTraversalCache.getApplicationsForProject(context, file.getProjectModel());
                if (fileApplications.contains(application)) {

                    ApplicationIgnoredFilesDto.IgnoredFileDto ignoredFileDto = new ApplicationIgnoredFilesDto.IgnoredFileDto();
                    ignoredFileDto.setFileName(file.getFileName());
                    ignoredFileDto.setFilePath(file.getFilePath());
                    ignoredFileDto.setReason(file.getIgnoredRegex());

                    filesDto.add(ignoredFileDto);
                }
            }

            ApplicationIgnoredFilesDto applicationIgnoredFilesDto = new ApplicationIgnoredFilesDto();
            applicationIgnoredFilesDto.setApplicationId(application.getId().toString());
            applicationIgnoredFilesDto.setIgnoredFiles(filesDto);

            result.add(applicationIgnoredFilesDto);
        }

        return result;
    }

    @Override
    public Map<String, Object> getById(GraphRewrite event) {
        return Collections.emptyMap();
    }

}
