package org.jboss.windup.reporting.rules.api;

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

    private List<FileData> getFileSources(GraphRewrite event, ProjectModelTraversal projectModelTraversal) {
        List<FileData> result = new ArrayList<>();

        for (FileModel fileModel : projectModelTraversal.getCanonicalProject().getFileModels()) {
            if (fileModel instanceof SourceFileModel && ((SourceFileModel) fileModel).isGenerateSourceReport()) {
                FileData sourceFileData = getSourceFileData(event, fileModel);
                result.add(sourceFileData);
            }
        }

        for (ProjectModelTraversal child : projectModelTraversal.getChildren()) {
            List<FileData> childrenSourceFiles = getFileSources(event, child);
            result.addAll(childrenSourceFiles);
        }

        return result;
    }

    private FileData getSourceFileData(GraphRewrite event, FileModel sourceFile) {
        SourceReportService sourceReportService = new SourceReportService(event.getGraphContext());
        SourceReportModel reportModel = sourceReportService.getSourceReportForFileModel(sourceFile);

        // Fill Data
        FileData result = new FileData();

        result.id = sourceFile.getId().toString();
        result.fullPath = reportModel.getProjectEdges().stream()
                .map(SourceReportToProjectEdgeModel::getFullPath)
                .collect(Collectors.joining(" | "));
        result.prettyPath = sourceFile.getPrettyPath();
        result.sourceType = resolveSourceType(sourceFile);
        result.fileContent = reportModel.getSourceBody();
        result.hints = reportModel.getSourceFileModel().getInlineHints().stream()
                .map(inlineHintModel -> {
                    HintData hintData = new HintData();

                    hintData.ruleId = inlineHintModel.getRuleID();
                    hintData.line = inlineHintModel.getLineNumber();
                    hintData.title = inlineHintModel.getTitle();
                    hintData.content = inlineHintModel.getHint();
                    hintData.links = inlineHintModel.getLinks().stream()
                            .map(linkModel -> {
                                LinkData linkData = new LinkData();
                                linkData.title = linkModel.getDescription();
                                linkData.href = linkModel.getLink();
                                return linkData;
                            })
                            .collect(Collectors.toList());

                    return hintData;
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

    static class FileData {
        public String id;
        public String fullPath;
        public String prettyPath;
        public String sourceType;
        public String fileContent;
        public List<HintData> hints;
    }

    static class HintData {
        public int line;
        public String title;
        public String ruleId;
        public String content;
        public List<LinkData> links;
    }

    static class LinkData {
        public String title;
        public String href;
    }
}
