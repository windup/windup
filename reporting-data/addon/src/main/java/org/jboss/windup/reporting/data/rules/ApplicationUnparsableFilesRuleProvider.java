package org.jboss.windup.reporting.data.rules;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.phase.ReportPf4RenderingPhase;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.WindupConfigurationService;
import org.jboss.windup.graph.traversal.ProjectModelTraversal;
import org.jboss.windup.reporting.data.dto.ApplicationUnparsableFilesDto;
import org.jboss.windup.reporting.model.source.SourceReportModel;
import org.jboss.windup.reporting.service.SourceReportService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RuleMetadata(
        phase = ReportPf4RenderingPhase.class,
        haltOnException = true
)
public class ApplicationUnparsableFilesRuleProvider extends AbstractApiRuleProvider {

    public static final String PATH = "unparsable-files";

    @Override
    public String getBasePath() {
        return "unparsable-files";
    }

    @Override
    public Object getAll(GraphRewrite event) {
        GraphContext context = event.getGraphContext();
        WindupConfigurationModel configurationModel = WindupConfigurationService.getConfigurationModel(context);
        SourceReportService sourceReportService = new SourceReportService(context);

        List<ApplicationUnparsableFilesDto> result = new ArrayList<>();

        for (FileModel inputPath : configurationModel.getInputPaths()) {
            ProjectModel application = inputPath.getProjectModel();


            ApplicationUnparsableFilesDto applicationUnparsableFilesDto = new ApplicationUnparsableFilesDto();
            applicationUnparsableFilesDto.setApplicationId(application.getId().toString());
            applicationUnparsableFilesDto.setSubProjects(getProjectsWithUnparsableFiles(new ProjectModelTraversal(application))
                    .stream()
                    .map(projectModel -> {
                        ApplicationUnparsableFilesDto.SubProjectDto subProjectDto = new ApplicationUnparsableFilesDto.SubProjectDto();
                        subProjectDto.setPath(projectModel.getRootFileModel().getPrettyPath());
                        subProjectDto.setUnparsableFiles(projectModel.getUnparsableFiles().stream()
                                .map(fileModel -> {
                                    SourceReportModel sourceReportModel = sourceReportService.getSourceReportForFileModel(fileModel);

                                    ApplicationUnparsableFilesDto.UnparsableFileDto unparsableFileDto = new ApplicationUnparsableFilesDto.UnparsableFileDto();
                                    unparsableFileDto.setFileName(fileModel.getFileName());
                                    unparsableFileDto.setFilePath(fileModel.getFilePath());
                                    unparsableFileDto.setParseError(fileModel.getParseError());

                                    if (sourceReportModel != null && sourceReportModel.getReportFilename() != null) {
                                        unparsableFileDto.setFileId(sourceReportModel.getSourceFileModel()
                                                .getId()
                                                .toString()
                                        );
                                    }

                                    return unparsableFileDto;
                                })
                                .collect(Collectors.toList())
                        );
                        return subProjectDto;
                    })
                    .collect(Collectors.toList())
            );

            result.add(applicationUnparsableFilesDto);
        }

        return result;
    }

    @Override
    public Map<String, Object> getById(GraphRewrite event) {
        return Collections.emptyMap();
    }

    private List<ProjectModel> getProjectsWithUnparsableFiles(ProjectModelTraversal traversal) {
        List<ProjectModel> results = new ArrayList<>();
        for (FileModel fileModel : traversal.getCanonicalProject().getUnparsableFiles()) {
            if (fileModel.getOnParseError() != FileModel.OnParseError.IGNORE) {
                results.add(traversal.getCanonicalProject());
                break;
            }
        }

        for (ProjectModelTraversal child : traversal.getChildren()) {
            results.addAll(getProjectsWithUnparsableFiles(child));
        }
        return results;
    }
}
