package org.jboss.windup.reporting.data.rules;

import org.jboss.forge.furnace.services.Imported;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.phase.ReportRenderingPhase;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.model.resource.SourceFileModel;
import org.jboss.windup.graph.service.WindupConfigurationService;
import org.jboss.windup.graph.traversal.ProjectModelTraversal;
import org.jboss.windup.reporting.SourceTypeResolver;
import org.jboss.windup.reporting.data.dto.FileContentDto;
import org.jboss.windup.reporting.data.dto.FileDto;
import org.jboss.windup.reporting.data.dto.HintDto;
import org.jboss.windup.reporting.data.dto.LinkDto;
import org.jboss.windup.reporting.model.source.SourceReportModel;
import org.jboss.windup.reporting.model.source.SourceReportToProjectEdgeModel;
import org.jboss.windup.reporting.service.SourceReportService;

import javax.inject.Inject;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
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

        // Fill Data
        FileDto fileDto = new FileDto();

        fileDto.id = sourceFile.getId().toString();
        fileDto.fullPath = reportModel.getProjectEdges().stream()
                .map(SourceReportToProjectEdgeModel::getFullPath)
                .collect(Collectors.joining(" | "));
        fileDto.prettyPath = sourceFile.getPrettyPath();
        fileDto.sourceType = resolveSourceType(sourceFile);
        fileDto.hints = reportModel.getSourceFileModel().getInlineHints().stream()
                .map(inlineHintModel -> {
                    HintDto hintDto = new HintDto();

                    hintDto.ruleId = inlineHintModel.getRuleID();
                    hintDto.line = inlineHintModel.getLineNumber();
                    hintDto.title = inlineHintModel.getTitle();
                    hintDto.content = inlineHintModel.getHint();
                    hintDto.links = inlineHintModel.getLinks().stream()
                            .map(linkModel -> {
                                LinkDto linkDto = new LinkDto();
                                linkDto.title = linkModel.getDescription();
                                linkDto.href = linkModel.getLink();
                                return linkDto;
                            })
                            .collect(Collectors.toList());

                    return hintDto;
                })
                .collect(Collectors.toList());

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
