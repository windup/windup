package org.jboss.windup.reporting.data.rules;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.janusgraph.core.attribute.Text;
import org.jboss.forge.furnace.services.Imported;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.phase.ReportPfRenderingPhase;
import org.jboss.windup.graph.frames.FramedVertexIterable;
import org.jboss.windup.graph.model.FileLocationModel;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.model.resource.SourceFileModel;
import org.jboss.windup.graph.service.WindupConfigurationService;
import org.jboss.windup.graph.traversal.ProjectModelTraversal;
import org.jboss.windup.reporting.SourceTypeResolver;
import org.jboss.windup.reporting.data.dto.ApplicationIssuesDto;
import org.jboss.windup.reporting.data.dto.FileContentDto;
import org.jboss.windup.reporting.data.dto.FileDto;
import org.jboss.windup.reporting.model.ClassificationModel;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@RuleMetadata(
        phase = ReportPfRenderingPhase.class,
        haltOnException = true
)
public class FilesRuleProvider extends AbstractApiRuleProvider {

    public static final String PATH = "files";

    @Inject
    private Imported<SourceTypeResolver> resolvers;

    @Override
    public String getBasePath() {
        return PATH;
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

                    contentDto.setId(fileDtoEntry.getKey().getId());
                    contentDto.setContent(fileDtoEntry.getValue());

                    result.put(contentDto.getId(), contentDto);
                });

        return result;
    }

    private Map<FileDto, String> getFileSources(GraphRewrite event, ProjectModelTraversal projectModelTraversal) {
        Map<FileDto, String> result = new HashMap<>();

        for (FileModel fileModel : projectModelTraversal.getCanonicalProject().getFileModels()) {
            if (fileModel instanceof SourceFileModel && ((SourceFileModel) fileModel).isGenerateSourceReport()) {
                AbstractMap.SimpleEntry<FileDto, Optional<String>> filesDto = getSourceFileData(event, fileModel);
                filesDto.getValue().ifPresent(fileContent -> {
                    result.put(filesDto.getKey(), fileContent);
                });
            }
        }

        for (ProjectModelTraversal child : projectModelTraversal.getChildren()) {
            Map<FileDto, String> childrenSourceFiles = getFileSources(event, child);
            result.putAll(childrenSourceFiles);
        }

        return result;
    }

    private AbstractMap.SimpleEntry<FileDto, Optional<String>> getSourceFileData(GraphRewrite event, FileModel sourceFile) {
        SourceReportService sourceReportService = new SourceReportService(event.getGraphContext());
        Optional<SourceReportModel> reportModel = Optional.ofNullable(sourceReportService.getSourceReportForFileModel(sourceFile));

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

        fileDto.setId(sourceFile.getId().toString());
        fileDto.setFullPath(reportModel
                .map(sourceReportModel -> sourceReportModel.getProjectEdges().stream()
                        .map(SourceReportToProjectEdgeModel::getFullPath)
                        .collect(Collectors.joining(" | "))
                )
                .orElse(null)
        );
        fileDto.setPrettyPath(sourceFile.getPrettyPath());
        fileDto.setPrettyFileName(IssuesRuleProvider.getPrettyPathForFile(sourceFile));
        fileDto.setSourceType(resolveSourceType(sourceFile));
        fileDto.setStoryPoints(storyPoints);

        // Hints
        List<FileDto.HintDto> hintDtoList = reportModel
                .map(sourceReportModel -> sourceReportModel.getSourceFileModel().getInlineHints().stream()
                        .map(inlineHintModel -> {
                            FileDto.HintDto hintDto = new FileDto.HintDto();

                            hintDto.setRuleId(inlineHintModel.getRuleID());
                            hintDto.setLine(inlineHintModel.getLineNumber());

                            hintDto.setTitle(inlineHintModel.getTitle());
                            hintDto.setContent(inlineHintModel.getHint());
                            hintDto.setLinks(inlineHintModel.getLinks().stream()
                                    .map(linkModel -> {
                                        ApplicationIssuesDto.LinkDto linkDto = new ApplicationIssuesDto.LinkDto();
                                        linkDto.setTitle(linkModel.getDescription());
                                        linkDto.setHref(linkModel.getLink());
                                        return linkDto;
                                    })
                                    .collect(Collectors.toList())
                            );

                            return hintDto;
                        })
                        .collect(Collectors.toList())
                )
                .orElse(Collections.emptyList());
        fileDto.setHints(hintDtoList);

        //
        fileDto.setTags(StreamSupport.stream(technologyTagsForFile.spliterator(), false)
                .map(technologyTagModel -> {
                    FileDto.TagDto tagDto = new FileDto.TagDto();
                    tagDto.setName(technologyTagModel.getName());
                    tagDto.setVersion(technologyTagModel.getVersion());
                    tagDto.setLevel(technologyTagModel.getLevel());
                    return tagDto;
                })
                .collect(Collectors.toList())
        );
        fileDto.setClassificationsAndHintsTags(Stream
                .concat(
                        StreamSupport.stream(classificationModels.spliterator(), false),
                        StreamSupport.stream(hintModels.spliterator(), false)
                )
                .map(TaggableModel::getTags)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet())
        );

        return new AbstractMap.SimpleEntry<>(fileDto, reportModel.map(SourceReportModel::getSourceBody));
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
