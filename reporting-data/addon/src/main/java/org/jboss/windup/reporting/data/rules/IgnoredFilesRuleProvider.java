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
import org.jboss.windup.reporting.data.dto.IgnoredFilesDto;

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

        List<IgnoredFilesDto> result = new ArrayList<>();

        for (FileModel inputPath : configurationModel.getInputPaths()) {
            ProjectModel application = inputPath.getProjectModel();
            GraphService<IgnoredFileModel> ignoredFilesModelService = new GraphService<>(context, IgnoredFileModel.class);

            List<IgnoredFilesDto.FileDto> filesDto = new ArrayList<>();
            for (IgnoredFileModel file : ignoredFilesModelService.findAll()) {
                Set<ProjectModel> fileApplications = ProjectTraversalCache.getApplicationsForProject(context, file.getProjectModel());
                if (fileApplications.contains(application)) {

                    IgnoredFilesDto.FileDto fileDto = new IgnoredFilesDto.FileDto();
                    fileDto.fileName = file.getFileName();
                    fileDto.filePath = file.getFilePath();
                    fileDto.reason = file.getIgnoredRegex();

                    filesDto.add(fileDto);
                }
            }

            IgnoredFilesDto ignoredFilesDto = new IgnoredFilesDto();
            ignoredFilesDto.applicationId = application.getId().toString();
            ignoredFilesDto.ignoredFiles = filesDto;

            result.add(ignoredFilesDto);
        }

        return result;
    }

    @Override
    public Map<String, Object> getById(GraphRewrite event) {
        return Collections.emptyMap();
    }

}
