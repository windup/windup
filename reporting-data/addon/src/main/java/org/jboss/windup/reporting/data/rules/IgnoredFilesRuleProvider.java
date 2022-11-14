package org.jboss.windup.reporting.data.rules;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.phase.ReportRenderingPhase;
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
        phase = ReportRenderingPhase.class,
        haltOnException = true
)
public class IgnoredFilesRuleProvider extends AbstractApiRuleProvider {

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

            List<ApplicationIgnoredFilesDto.FileDto> filesDto = new ArrayList<>();
            for (IgnoredFileModel file : ignoredFilesModelService.findAll()) {
                Set<ProjectModel> fileApplications = ProjectTraversalCache.getApplicationsForProject(context, file.getProjectModel());
                if (fileApplications.contains(application)) {

                    ApplicationIgnoredFilesDto.FileDto fileDto = new ApplicationIgnoredFilesDto.FileDto();
                    fileDto.fileName = file.getFileName();
                    fileDto.filePath = file.getFilePath();
                    fileDto.reason = file.getIgnoredRegex();

                    filesDto.add(fileDto);
                }
            }

            ApplicationIgnoredFilesDto applicationIgnoredFilesDto = new ApplicationIgnoredFilesDto();
            applicationIgnoredFilesDto.applicationId = application.getId().toString();
            applicationIgnoredFilesDto.ignoredFiles = filesDto;

            result.add(applicationIgnoredFilesDto);
        }

        return result;
    }

    @Override
    public Map<String, Object> getById(GraphRewrite event) {
        return Collections.emptyMap();
    }

}
