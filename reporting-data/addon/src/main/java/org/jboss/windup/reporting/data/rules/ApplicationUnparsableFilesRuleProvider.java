package org.jboss.windup.reporting.data.rules;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.phase.ReportRenderingPhase;
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
        phase = ReportRenderingPhase.class,
        haltOnException = true
)
public class ApplicationUnparsableFilesRuleProvider extends AbstractApiRuleProvider {

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
            applicationUnparsableFilesDto.applicationId = application.getId().toString();
            applicationUnparsableFilesDto.subProjects = getProjectsWithUnparsableFiles(new ProjectModelTraversal(application))
                    .stream()
                    .map(projectModel -> {
                        ApplicationUnparsableFilesDto.SubProjectDto subProjectDto = new ApplicationUnparsableFilesDto.SubProjectDto();
                        subProjectDto.path = projectModel.getRootFileModel().getPrettyPath();
                        projectModel.getUnparsableFiles().stream()
                                .map(fileModel -> {
                                    SourceReportModel sourceReportModel = sourceReportService.getSourceReportForFileModel(fileModel);

                                    ApplicationUnparsableFilesDto.UnparsableFileDto unparsableFileDto = new ApplicationUnparsableFilesDto.UnparsableFileDto();
                                    unparsableFileDto.fileName = fileModel.getFileName();
                                    unparsableFileDto.filePath = fileModel.getFilePath();
                                    unparsableFileDto.parseError = fileModel.getParseError();

                                    if (sourceReportModel != null && sourceReportModel.getReportFilename() != null) {
                                        unparsableFileDto.fileId = sourceReportModel.getSourceFileModel()
                                                .getId()
                                                .toString();
                                    }

                                    return unparsableFileDto;
                                });
                        return subProjectDto;
                    })
                    .collect(Collectors.toList());

            result.add(applicationUnparsableFilesDto);
        }

        return result;
    }

    @Override
    public Map<String, Object> getById(GraphRewrite event) {
        return Collections.emptyMap();
    }

//    private List<ApplicationUnparsableFilesDto.SubProjectDto> getSubProjects(GraphContext context , ProjectModel application) {
//        SourceReportService sourceReportService = new SourceReportService(context);
//        ProjectModelTraversal traversal = new ProjectModelTraversal(application);
//
//        traversal.getCanonicalProject().getUnparsableFiles().stream()
//                .filter(fileModel -> !Objects.equals(FileModel.OnParseError.IGNORE, fileModel.getOnParseError()))
//                .map(fileModel -> {
//                    ProjectModel canonicalProject = traversal.getCanonicalProject();
//
//                    ApplicationUnparsableFilesDto.SubProjectDto subProjectDto = new ApplicationUnparsableFilesDto.SubProjectDto();
//                    subProjectDto.path = canonicalProject.getRootFileModel().getPrettyPath();
//                    application.getUnparsableFiles().forEach(fileModel1 -> {
//
//                    });
//
//                    SourceReportModel result = sourceReportService.getSourceReportForFileModel(fileModel);
//                    result.getReportFilename();
//
//                    fileModel.getFileName();
//                    fileModel.getFilePath();
//                    fileModel.getOnParseError();
//                    fileModel.getParseError();
//                    return subProjectDto;
//                });
//    }

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
