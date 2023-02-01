package org.jboss.windup.reporting.data.rules;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.phase.ReportPf4RenderingPhase;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ArchiveModel;
import org.jboss.windup.graph.model.OrganizationModel;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.graph.service.WindupConfigurationService;
import org.jboss.windup.graph.traversal.AllTraversalStrategy;
import org.jboss.windup.graph.traversal.ArchiveSHA1ToFilePathMapper;
import org.jboss.windup.graph.traversal.OnlyOnceTraversalStrategy;
import org.jboss.windup.graph.traversal.ProjectModelTraversal;
import org.jboss.windup.reporting.data.dto.ApplicationDetailsDto;
import org.jboss.windup.reporting.model.OverviewReportLineMessageModel;
import org.jboss.windup.reporting.service.ClassificationService;
import org.jboss.windup.reporting.service.InlineHintService;
import org.jboss.windup.reporting.service.SourceReportService;
import org.jboss.windup.rules.apps.java.archives.model.IdentifiedArchiveModel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RuleMetadata(
        phase = ReportPf4RenderingPhase.class,
        haltOnException = true
)
public class ApplicationDetailsProvider extends AbstractApiRuleProvider {

    @Override
    public String getBasePath() {
        return "applications-details";
    }

    @Override
    public Object getAll(GraphRewrite event) {
        WindupConfigurationModel configurationModel = WindupConfigurationService.getConfigurationModel(event.getGraphContext());
        GraphContext context = event.getGraphContext();

        List<ApplicationDetailsDto> result = new ArrayList<>();
        for (FileModel inputPath : configurationModel.getInputPaths()) {
            ProjectModel projectModel = inputPath.getProjectModel();

            ArchiveSHA1ToFilePathMapper sha1ToPathsMapper = new ArchiveSHA1ToFilePathMapper(
                    new ProjectModelTraversal(projectModel, new AllTraversalStrategy())
            );

            //
            ApplicationDetailsDto applicationDetailsDto = new ApplicationDetailsDto();
            applicationDetailsDto.applicationId = projectModel.getId().toString();
            applicationDetailsDto.messages = getMessages(context, projectModel);
            applicationDetailsDto.applicationFiles = toFileDto(
                    context,
                    new ProjectModelTraversal(projectModel, new OnlyOnceTraversalStrategy()),
                    sha1ToPathsMapper
            );

            result.add(applicationDetailsDto);
        }
        return result;
    }

    @Override
    public Map<String, Object> getById(GraphRewrite event) {
        return Collections.emptyMap();
    }

    private List<ApplicationDetailsDto.MessageDto> getMessages(
            GraphContext context,
            ProjectModel projectModel
    ) {
        List<ApplicationDetailsDto.MessageDto> result = new ArrayList<>();

        ProjectModelTraversal projectModelTraversal = new ProjectModelTraversal(projectModel);
        Set<ProjectModel> allProjectsInApplication = projectModelTraversal.getAllProjects(true);

        GraphService<OverviewReportLineMessageModel> lineNotesService = new GraphService<>(context, OverviewReportLineMessageModel.class);
        Iterable<OverviewReportLineMessageModel> allLines = lineNotesService.findAll();

        Set<String> dupeCheck = new HashSet<>();
        for (OverviewReportLineMessageModel line : allLines) {
            if (dupeCheck.contains(line.getMessage())) {
                continue;
            }

            ProjectModel project = line.getProject();
            if (allProjectsInApplication.contains(project)) {
                dupeCheck.add(line.getMessage());

                ApplicationDetailsDto.MessageDto message = new ApplicationDetailsDto.MessageDto();
                message.value = line.getMessage();
                message.ruleId = line.getRuleID();
                result.add(message);
            }
        }

        return result;
    }

    private List<ApplicationDetailsDto.ApplicationFileDto> toFileDto(
            GraphContext context,
            ProjectModelTraversal traversal,
            ArchiveSHA1ToFilePathMapper sha1ToPathsMapper) {
        List<ApplicationDetailsDto.ApplicationFileDto> result = new ArrayList<>();

        ProjectModel projectModel = traversal.getCurrent();
        ProjectModel canonicalProject = traversal.getCanonicalProject();

        List<String> duplicatePaths = sha1ToPathsMapper.getPathsBySHA1(projectModel.getRootFileModel().getSHA1Hash());
        String fileName = projectModel.getRootFileModel().getFileName();
        String rootPath = traversal.getFilePath(projectModel.getRootFileModel());

        // Story points
        ClassificationService classificationService = new ClassificationService(context);
        InlineHintService inlineHintService = new InlineHintService(context);
        ProjectModelTraversal storyPointsTraversal = new ProjectModelTraversal(projectModel, new AllTraversalStrategy());

        Map<Integer, Integer> classificationEffortDetails = classificationService.getMigrationEffortByPoints(storyPointsTraversal, Collections.emptySet(), Collections.emptySet(), Collections.emptySet(), false, false);
        Map<Integer, Integer> hintEffortDetails = inlineHintService.getMigrationEffortByPoints(storyPointsTraversal, Collections.emptySet(), Collections.emptySet(), Collections.emptySet(), false, false);
        Map<Integer, Integer> results = ApplicationsApiRuleProvider.sumMaps(classificationEffortDetails, hintEffortDetails);
        int storyPoints = ApplicationsApiRuleProvider.sumPoints(results);

        // Issues
        SourceReportService sourceReportService = new SourceReportService(context);
        List<String> childrenFiles = canonicalProject.getFileModelsNoDirectories().stream()
                .map(sourceReportService::getSourceReportForFileModel)
                .filter(Objects::nonNull)
                .map(sourceReportModel -> sourceReportModel.getSourceFileModel().getId().toString())
                .collect(Collectors.toList());

        // Map values
        ApplicationDetailsDto.ApplicationFileDto fileDto = new ApplicationDetailsDto.ApplicationFileDto();
        fileDto.fileId = projectModel.getRootFileModel().getId().toString();
        fileDto.fileName = fileName;
        fileDto.rootPath = rootPath;
        fileDto.storyPoints = storyPoints;
        fileDto.childrenFileIds = childrenFiles;

        fileDto.maven = new ApplicationDetailsDto.MavenDto();
        fileDto.maven.mavenIdentifier = canonicalProject.getProperty("mavenIdentifier");
        fileDto.maven.projectSite = canonicalProject.getURL();
        fileDto.maven.name = canonicalProject.getName();
        fileDto.maven.version = canonicalProject.getVersion();
        fileDto.maven.description = canonicalProject.getDescription();
        fileDto.maven.duplicatePaths = duplicatePaths.size() > 1 ? duplicatePaths : null;
        if (canonicalProject.getRootFileModel() instanceof ArchiveModel) {
            fileDto.maven.organizations = ((ArchiveModel) canonicalProject.getRootFileModel()).getOrganizationModels()
                    .stream().map(OrganizationModel::getName)
                    .collect(Collectors.toList());
        }
        if (canonicalProject.getRootFileModel() instanceof IdentifiedArchiveModel) {
            fileDto.maven.sha1 = canonicalProject.getRootFileModel().getSHA1Hash();
        }

        result.add(fileDto);

        // Children
        List<ApplicationDetailsDto.ApplicationFileDto> childrenFileDtos = StreamSupport.stream(traversal.getChildren().spliterator(), false)
                .map(p -> toFileDto(context, p, sha1ToPathsMapper))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        result.addAll(childrenFileDtos);

        return result;
    }
}
