package org.jboss.windup.reporting.data.rules;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.phase.ReportPfRenderingPhase;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ArchiveModel;
import org.jboss.windup.graph.model.OrganizationModel;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.model.comparator.ProjectTraversalRootFileComparator;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.graph.service.WindupConfigurationService;
import org.jboss.windup.graph.traversal.AllTraversalStrategy;
import org.jboss.windup.graph.traversal.ArchiveSHA1ToFilePathMapper;
import org.jboss.windup.graph.traversal.OnlyOnceTraversalStrategy;
import org.jboss.windup.graph.traversal.ProjectModelTraversal;
import org.jboss.windup.reporting.data.dto.ApplicationDetailsDto;
import org.jboss.windup.reporting.data.rules.utils.DataUtils;
import org.jboss.windup.reporting.model.OverviewReportLineMessageModel;
import org.jboss.windup.reporting.model.TaggableModel;
import org.jboss.windup.reporting.service.ClassificationService;
import org.jboss.windup.reporting.service.InlineHintService;
import org.jboss.windup.reporting.service.SourceReportService;
import org.jboss.windup.rules.apps.java.archives.model.IdentifiedArchiveModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RuleMetadata(
        phase = ReportPfRenderingPhase.class,
        haltOnException = true
)
public class ApplicationDetailsRuleProvider extends AbstractApiRuleProvider {

    public static final String PATH = "applications-details";

    @Override
    public String getBasePath() {
        return PATH;
    }

    @Override
    public Object getAll(GraphRewrite event) {
        WindupConfigurationModel configurationModel = WindupConfigurationService.getConfigurationModel(event.getGraphContext());
        GraphContext context = event.getGraphContext();

        ClassificationService classificationService = new ClassificationService(context);
        InlineHintService inlineHintService = new InlineHintService(context);
        SourceReportService sourceReportService = new SourceReportService(context);

        List<ApplicationDetailsDto> result = new ArrayList<>();
        for (FileModel inputPath : configurationModel.getInputPaths()) {
            ProjectModel projectModel = inputPath.getProjectModel();

            // Collect children traversals
            List<ProjectModelTraversal> projectModelTraversals = collectTraversalChildren(new ProjectModelTraversal(projectModel, new OnlyOnceTraversalStrategy()), new ArrayList<>());
            projectModelTraversals.sort(new ProjectTraversalRootFileComparator());

            ArchiveSHA1ToFilePathMapper sha1ToPathsMapper = new ArchiveSHA1ToFilePathMapper(
                    new ProjectModelTraversal(projectModel, new AllTraversalStrategy())
            );

            // DTO definition
            ApplicationDetailsDto applicationDetailsDto = new ApplicationDetailsDto();
            applicationDetailsDto.setApplicationId(projectModel.getId().toString());
            applicationDetailsDto.setMessages(getMessages(context, projectModel));
            applicationDetailsDto.setApplicationFiles(projectModelTraversals.parallelStream()
                    .map(traversal -> traversalToDto(sourceReportService, classificationService, inlineHintService, traversal, sha1ToPathsMapper))
                    .collect(Collectors.toList())
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
                message.setValue(line.getMessage());
                message.setRuleId(line.getRuleID());
                result.add(message);
            }
        }

        return result;
    }

    private List<ProjectModelTraversal> collectTraversalChildren(
            ProjectModelTraversal traversal,
            List<ProjectModelTraversal> accumulator
    ) {
        accumulator.add(traversal);

        StreamSupport.stream(traversal.getChildren().spliterator(), false)
                .forEach(childTraversal -> collectTraversalChildren(childTraversal, accumulator));

        return accumulator;
    }

    public ApplicationDetailsDto.ApplicationFileDto traversalToDto(
            SourceReportService sourceReportService,
            ClassificationService classificationService,
            InlineHintService inlineHintService,
            ProjectModelTraversal traversal,
            ArchiveSHA1ToFilePathMapper sha1ToPathsMapper
    ) {
        ProjectModel projectModel = traversal.getCurrent();
        ProjectModel canonicalProject = traversal.getCanonicalProject();
        String rootFilePath = traversal.getFilePath(projectModel.getRootFileModel());
        List<String> duplicatePaths = sha1ToPathsMapper.getPathsBySHA1(projectModel.getRootFileModel().getSHA1Hash());

        String fileName = projectModel.getRootFileModel().getFileName();

        // Children files
        List<String> childrenFiles = canonicalProject.getFileModelsNoDirectories().stream()
                .map(fileModel -> DataUtils.getSourceFileId(sourceReportService, fileModel))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // Story points
        int storyPoints = getMigrationEffortPointsForProject(classificationService, inlineHintService, traversal);

        // DTO
        ApplicationDetailsDto.ApplicationFileDto result = new ApplicationDetailsDto.ApplicationFileDto();
        result.setFileId(projectModel.getRootFileModel().getId().toString());
        result.setFileName(fileName);
        result.setRootPath(rootFilePath);
        result.setStoryPoints(storyPoints);
        result.setChildrenFileIds(childrenFiles);

        result.setMaven(new ApplicationDetailsDto.MavenDto());
        result.getMaven().setMavenIdentifier(canonicalProject.getProperty("mavenIdentifier"));
        result.getMaven().setProjectSite(canonicalProject.getURL());
        result.getMaven().setName(canonicalProject.getName());
        result.getMaven().setVersion(canonicalProject.getVersion());
        result.getMaven().setDescription(canonicalProject.getDescription());
        result.getMaven().setDuplicatePaths(duplicatePaths.size() > 1 ? duplicatePaths : null);
        if (canonicalProject.getRootFileModel() instanceof ArchiveModel) {
            result.getMaven().setOrganizations(((ArchiveModel) canonicalProject.getRootFileModel()).getOrganizationModels()
                    .stream().map(OrganizationModel::getName)
                    .collect(Collectors.toList())
            );
        }
        if (canonicalProject.getRootFileModel() instanceof IdentifiedArchiveModel) {
            result.getMaven().setSha1(canonicalProject.getRootFileModel().getSHA1Hash());
        }

        return result;
    }

    public int getMigrationEffortPointsForProject(
            ClassificationService classificationService,
            InlineHintService inlineHintService,
            ProjectModelTraversal traversal
    ) {
        Set<String> includedTags = Collections.emptySet();
        Set<String> excludedTags = Collections.singleton(TaggableModel.CATCHALL_TAG);
        Set<String> issueCategories = Collections.emptySet();

        Map<Integer, Integer> classificationEffortDetails = classificationService.getMigrationEffortByPoints(traversal, includedTags, excludedTags, issueCategories, false, false);
        Map<Integer, Integer> hintEffortDetails = inlineHintService.getMigrationEffortByPoints(traversal, includedTags, excludedTags, issueCategories, false, false);
        Map<Integer, Integer> results = ApplicationsRuleProvider.sumMaps(classificationEffortDetails, hintEffortDetails);

        return ApplicationsRuleProvider.sumPoints(results);
    }
}
