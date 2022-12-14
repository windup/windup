package org.jboss.windup.reporting.data.rules;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.janusgraph.core.attribute.Text;
import org.jboss.forge.furnace.services.Imported;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.phase.ReportRenderingPhase;
import org.jboss.windup.graph.frames.FramedVertexIterable;
import org.jboss.windup.graph.model.FileLocationModel;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.model.resource.SourceFileModel;
import org.jboss.windup.graph.service.WindupConfigurationService;
import org.jboss.windup.graph.traversal.ProjectModelTraversal;
import org.jboss.windup.reporting.SourceTypeResolver;
import org.jboss.windup.reporting.data.dto.ApplicationIssueDto;
import org.jboss.windup.reporting.data.dto.FileContentDto;
import org.jboss.windup.reporting.data.dto.FileDto;
import org.jboss.windup.reporting.model.ClassificationModel;
import org.jboss.windup.reporting.model.EffortReportModel;
import org.jboss.windup.reporting.model.InlineHintModel;
import org.jboss.windup.reporting.model.TaggableModel;
import org.jboss.windup.reporting.model.TechnologyTagModel;
import org.jboss.windup.reporting.model.source.SourceReportModel;
import org.jboss.windup.reporting.model.source.SourceReportToProjectEdgeModel;
import org.jboss.windup.reporting.service.ClassificationService;
import org.jboss.windup.reporting.service.InlineHintService;
import org.jboss.windup.reporting.service.SourceReportService;
import org.jboss.windup.reporting.service.TechnologyTagService;

import javax.inject.Inject;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@RuleMetadata(
        phase = ReportRenderingPhase.class,
        haltOnException = true
)
public class FilesApiRuleProvider extends AbstractApiRuleProvider {

    @Inject
    private Imported<SourceTypeResolver> resolvers;

    @Override
    public String getBasePath() {
        return "files";
    }

    @Override
    public Object getAll(GraphRewrite event) {
        WindupConfigurationModel configurationModel = WindupConfigurationService.getConfigurationModel(event.getGraphContext());
        Iterable<FileModel> inputApplications = configurationModel.getInputPaths();

        return StreamSupport.stream(inputApplications.spliterator(), false)
                .map(inputApplication -> {
                    ProjectModelTraversal projectModelTraversal = new ProjectModelTraversal(inputApplication.getProjectModel());
                    return getFileSources(event, projectModelTraversal);
                })
                .map(Map::keySet)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getById(GraphRewrite event) {
        WindupConfigurationModel configurationModel = WindupConfigurationService.getConfigurationModel(event.getGraphContext());
        Iterable<FileModel> inputApplications = configurationModel.getInputPaths();

        Map<String, Object> result = new HashMap<>();
        StreamSupport.stream(inputApplications.spliterator(), false)
                .map(inputApplication -> {
                    ProjectModelTraversal projectModelTraversal = new ProjectModelTraversal(inputApplication.getProjectModel());
                    return getFileSources(event, projectModelTraversal);
                })
                .map(Map::entrySet)
                .flatMap(Collection::stream)
                .forEach(fileDtoEntry -> {
                    FileContentDto contentDto = new FileContentDto();

                    contentDto.id = fileDtoEntry.getKey().id;
                    contentDto.content = fileDtoEntry.getValue();

                    result.put(contentDto.id, contentDto);
                });

        return result;
    }

    private Map<FileDto, String> getFileSources(GraphRewrite event, ProjectModelTraversal projectModelTraversal) {
        Map<FileDto, String> result = new HashMap<>();

        for (FileModel fileModel : projectModelTraversal.getCanonicalProject().getFileModels()) {
            if (fileModel instanceof SourceFileModel && ((SourceFileModel) fileModel).isGenerateSourceReport()) {
                AbstractMap.SimpleEntry<FileDto, String> filesDto = getSourceFileData(event, fileModel);
                result.put(filesDto.getKey(), filesDto.getValue());
            }
        }

        for (ProjectModelTraversal child : projectModelTraversal.getChildren()) {
            Map<FileDto, String> childrenSourceFiles = getFileSources(event, child);
            result.putAll(childrenSourceFiles);
        }

        return result;
    }

    private AbstractMap.SimpleEntry<FileDto, String> getSourceFileData(GraphRewrite event, FileModel sourceFile) {
        SourceReportService sourceReportService = new SourceReportService(event.getGraphContext());
        SourceReportModel reportModel = sourceReportService.getSourceReportForFileModel(sourceFile);

        Iterable<TechnologyTagModel> technologyTagsForFile = new TechnologyTagService(event.getGraphContext())
                .findTechnologyTagsForFile(sourceFile);

        // Classifications
        GraphTraversal<Vertex, Vertex> classificationPipeline = new GraphTraversalSource(event.getGraphContext().getGraph()).V(sourceFile.getElement());
        classificationPipeline.in(ClassificationModel.FILE_MODEL).has(WindupVertexFrame.TYPE_PROP, Text.textContains(ClassificationModel.TYPE));
        FramedVertexIterable<ClassificationModel> classificationModels = new FramedVertexIterable<>(event.getGraphContext().getFramed(), classificationPipeline.toList(), ClassificationModel.class);

        // Hints
        GraphTraversal<Vertex, Vertex> hintPipeline = new GraphTraversalSource(event.getGraphContext().getGraph()).V(sourceFile.getElement());
        hintPipeline.in(FileLocationModel.FILE_MODEL).has(WindupVertexFrame.TYPE_PROP, Text.textContains(FileLocationModel.TYPE));
        hintPipeline.in(InlineHintModel.FILE_LOCATION_REFERENCE).has(WindupVertexFrame.TYPE_PROP, Text.textContains(InlineHintModel.TYPE));
        FramedVertexIterable<InlineHintModel> hintModels = new FramedVertexIterable<>(event.getGraphContext().getFramed(), hintPipeline.toList(), InlineHintModel.class);

        // Story points
        ClassificationService classificationService = new ClassificationService(event.getGraphContext());
        InlineHintService inlineHintService = new InlineHintService(event.getGraphContext());
        int storyPoints = classificationService.getMigrationEffortPoints(sourceFile) + inlineHintService.getMigrationEffortPoints(sourceFile);

        // Fill Data
        FileDto fileDto = new FileDto();

        fileDto.id = sourceFile.getId().toString();
        fileDto.fullPath = reportModel.getProjectEdges().stream()
                .map(SourceReportToProjectEdgeModel::getFullPath)
                .collect(Collectors.joining(" | "));
        fileDto.prettyPath = sourceFile.getPrettyPath();
        fileDto.prettyFileName = IssuesApiRuleProvider.getPrettyPathForFile(sourceFile);
        fileDto.sourceType = resolveSourceType(sourceFile);
        fileDto.storyPoints = storyPoints;
        fileDto.hints = reportModel.getSourceFileModel().getInlineHints().stream()
                .map(inlineHintModel -> {
                    FileDto.HintDto hintDto = new FileDto.HintDto();

                    hintDto.ruleId = inlineHintModel.getRuleID();
                    hintDto.line = inlineHintModel.getLineNumber();
                    hintDto.title = inlineHintModel.getTitle();
                    hintDto.content = inlineHintModel.getHint();
                    hintDto.links = inlineHintModel.getLinks().stream()
                            .map(linkModel -> {
                                ApplicationIssueDto.LinkDto linkDto = new ApplicationIssueDto.LinkDto();
                                linkDto.title = linkModel.getDescription();
                                linkDto.href = linkModel.getLink();
                                return linkDto;
                            })
                            .collect(Collectors.toList());

                    return hintDto;
                })
                .collect(Collectors.toList());
        fileDto.tags = StreamSupport.stream(technologyTagsForFile.spliterator(), false)
                .map(technologyTagModel -> {
                    FileDto.TagDto tagDto = new FileDto.TagDto();
                    tagDto.name = technologyTagModel.getName();
                    tagDto.version = technologyTagModel.getVersion();
                    tagDto.level = technologyTagModel.getLevel();
                    return tagDto;
                })
                .collect(Collectors.toList());
        fileDto.classificationsAndHintsTags = Stream
                .concat(
                        StreamSupport.stream(classificationModels.spliterator(), false),
                        StreamSupport.stream(hintModels.spliterator(), false)
                )
                .map(TaggableModel::getTags)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());

        return new AbstractMap.SimpleEntry<>(fileDto, reportModel.getSourceBody());
    }

    private String resolveSourceType(FileModel f) {
        for (SourceTypeResolver resolver : resolvers) {
            String resolvedType = resolver.resolveSourceType(f);
            if (resolvedType != null) {
                return resolvedType;
            }
        }
        return "unknown";
    }
}
