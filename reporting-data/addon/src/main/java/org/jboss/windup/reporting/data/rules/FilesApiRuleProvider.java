package org.jboss.windup.reporting.data.rules;

import org.jboss.forge.furnace.services.Imported;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.phase.PostReportGenerationPhase;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.model.resource.SourceFileModel;
import org.jboss.windup.graph.service.WindupConfigurationService;
import org.jboss.windup.graph.traversal.ProjectModelTraversal;
import org.jboss.windup.reporting.SourceTypeResolver;
import org.jboss.windup.reporting.data.dto.FileDto;
import org.jboss.windup.reporting.data.dto.HintDto;
import org.jboss.windup.reporting.data.dto.LinkDto;
import org.jboss.windup.reporting.model.source.SourceReportModel;
import org.jboss.windup.reporting.model.source.SourceReportToProjectEdgeModel;
import org.jboss.windup.reporting.rules.AttachApplicationReportsToIndexRuleProvider;
import org.jboss.windup.reporting.service.SourceReportService;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RuleMetadata(
        phase = PostReportGenerationPhase.class,
        before = AttachApplicationReportsToIndexRuleProvider.class,
        haltOnException = true
)
public class FilesApiRuleProvider extends AbstractApiRuleProvider {

    @Inject
    private Imported<SourceTypeResolver> resolvers;

    @Override
    public String getOutputFilename() {
        return "files.json";
    }

    @Override
    public Object getData(GraphRewrite event) {
        WindupConfigurationModel configurationModel = WindupConfigurationService.getConfigurationModel(event.getGraphContext());
        Iterable<FileModel> inputApplications = configurationModel.getInputPaths();

        return StreamSupport.stream(inputApplications.spliterator(), false)
                .map(inputApplication -> {
                    ProjectModelTraversal projectModelTraversal = new ProjectModelTraversal(inputApplication.getProjectModel());
                    return getFileSources(event, projectModelTraversal);
                }).flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    private List<FileDto> getFileSources(GraphRewrite event, ProjectModelTraversal projectModelTraversal) {
        List<FileDto> result = new ArrayList<>();

        for (FileModel fileModel : projectModelTraversal.getCanonicalProject().getFileModels()) {
            if (fileModel instanceof SourceFileModel && ((SourceFileModel) fileModel).isGenerateSourceReport()) {
                FileDto filesDto = getSourceFileData(event, fileModel);
                result.add(filesDto);
            }
        }

        for (ProjectModelTraversal child : projectModelTraversal.getChildren()) {
            List<FileDto> childrenSourceFiles = getFileSources(event, child);
            result.addAll(childrenSourceFiles);
        }

        return result;
    }

    private FileDto getSourceFileData(GraphRewrite event, FileModel sourceFile) {
        SourceReportService sourceReportService = new SourceReportService(event.getGraphContext());
        SourceReportModel reportModel = sourceReportService.getSourceReportForFileModel(sourceFile);

        // Fill Data
        FileDto result = new FileDto();

        result.id = sourceFile.getId().toString();
        result.fullPath = reportModel.getProjectEdges().stream()
                .map(SourceReportToProjectEdgeModel::getFullPath)
                .collect(Collectors.joining(" | "));
        result.prettyPath = sourceFile.getPrettyPath();
        result.sourceType = resolveSourceType(sourceFile);
        result.fileContent = reportModel.getSourceBody();
        result.hints = reportModel.getSourceFileModel().getInlineHints().stream()
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

        return result;
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
