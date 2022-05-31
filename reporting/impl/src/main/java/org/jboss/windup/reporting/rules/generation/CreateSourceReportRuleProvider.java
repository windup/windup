package org.jboss.windup.reporting.rules.generation;

import org.jboss.forge.furnace.services.Imported;
import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.operation.GraphOperation;
import org.jboss.windup.config.phase.PostReportGenerationPhase;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.model.resource.SourceFileModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.graph.service.WindupConfigurationService;
import org.jboss.windup.graph.traversal.ProjectModelTraversal;
import org.jboss.windup.reporting.SourceTypeResolver;
import org.jboss.windup.reporting.model.ApplicationReportModel;
import org.jboss.windup.reporting.model.FreeMarkerSourceReportModel;
import org.jboss.windup.reporting.model.ReportFileModel;
import org.jboss.windup.reporting.model.TemplateType;
import org.jboss.windup.reporting.model.source.SourceReportModel;
import org.jboss.windup.reporting.model.source.SourceReportToProjectEdgeModel;
import org.jboss.windup.reporting.service.ApplicationReportService;
import org.jboss.windup.reporting.service.ReportService;
import org.jboss.windup.reporting.service.SourceReportService;
import org.jboss.windup.util.Logging;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

import javax.inject.Inject;
import java.util.logging.Logger;

/**
 * This creates SourceReportModel entries for every relevant item within the graph.
 * <p>
 * Relevancy is based on whether the item has classifications or blacklists attached to it.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
@RuleMetadata(phase = PostReportGenerationPhase.class)
public class CreateSourceReportRuleProvider extends AbstractRuleProvider {
    private static final Logger LOG = Logging.get(CreateSourceReportRuleProvider.class);
    private static final String TEMPLATE = "/reports/templates/source.ftl";

    @Inject
    private Imported<SourceTypeResolver> resolvers;

    // @formatter:off
    @Override
    public Configuration getConfiguration(RuleLoaderContext ruleLoaderContext) {
        GraphOperation addSourceReports = new GraphOperation() {
            @Override
            public void perform(GraphRewrite event, EvaluationContext context) {
                WindupConfigurationModel configurationModel = WindupConfigurationService.getConfigurationModel(event.getGraphContext());
                ReportService reportService = new ReportService(event.getGraphContext());
                Iterable<FileModel> inputApplications = configurationModel.getInputPaths();
                for (FileModel inputApplication : inputApplications) {
                    ProjectModelTraversal projectModelTraversal = new ProjectModelTraversal(inputApplication.getProjectModel());
                    traverse(event, projectModelTraversal, reportService);
                }
            }

            @Override
            public String toString() {
                return "AddSourceReport";
            }
        };

        return ConfigurationBuilder.begin()
                .addRule()
                .perform(addSourceReports);
    }
    // @formatter:on

    private void traverse(GraphRewrite event, ProjectModelTraversal traversal, ReportService reportService) {
        for (FileModel fileModel : traversal.getCanonicalProject().getFileModels()) {
            if (fileModel instanceof SourceFileModel && ((SourceFileModel) fileModel).isGenerateSourceReport())
                createSourceReport(event, traversal, reportService, fileModel);
        }

        for (ProjectModelTraversal child : traversal.getChildren()) {
            traverse(event, child, reportService);
        }
    }

    private void createSourceReport(GraphRewrite event, ProjectModelTraversal traversal, ReportService reportService, FileModel sourceFile) {
        ProjectModel application = traversal.getCurrent().getRootProjectModel();
        SourceReportService sourceReportService = new SourceReportService(
                event.getGraphContext());
        SourceReportModel sourceReportModel = sourceReportService.getSourceReportForFileModel(sourceFile);
        if (sourceReportModel != null) {
            for (SourceReportToProjectEdgeModel existing : sourceReportModel.getProjectEdges()) {
                if (existing.getProjectModel().equals(application))
                    return;
            }

            // just add another project to this report
            SourceReportToProjectEdgeModel toProjectEdge = sourceReportModel.addProjectModel(application);
            toProjectEdge.setFullPath(traversal.getFilePath(sourceFile));
            return;
        }

        sourceReportModel = sourceReportService.create();

        ReportFileModel reportFileModel = GraphService.addTypeToModel(event.getGraphContext(), sourceFile,
                ReportFileModel.class);
        sourceReportModel.setSourceFileModel(reportFileModel);

        SourceReportToProjectEdgeModel toProjectEdge = sourceReportModel.addProjectModel(application);
        toProjectEdge.setFullPath(traversal.getFilePath(sourceFile));

        sourceReportModel.setReportName(sourceFile.getPrettyPath());
        sourceReportModel.setSourceType(resolveSourceType(sourceFile));

        sourceReportModel.setReportName(sourceFile.getFileName());
        sourceReportModel.setTemplatePath(TEMPLATE);
        sourceReportModel.setTemplateType(TemplateType.FREEMARKER);
        ApplicationReportService applicationReportService = new ApplicationReportService(event.getGraphContext());
        ApplicationReportModel mainAppReport = applicationReportService.getMainApplicationReportForFile(sourceFile);
        if (mainAppReport != null) {
            sourceReportModel.setParentReport(mainAppReport);
        }

        GraphService.addTypeToModel(event.getGraphContext(), sourceReportModel, FreeMarkerSourceReportModel.class);
        reportService.setUniqueFilename(sourceReportModel, sourceFile.getFileName(), "html");
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
