package org.jboss.windup.rules.apps.java.reporting.rules;

import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.operation.GraphOperation;
import org.jboss.windup.config.phase.ReportGenerationPhase;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.graph.service.WindupConfigurationService;
import org.jboss.windup.graph.traversal.ProjectModelTraversal;
import org.jboss.windup.reporting.model.TemplateType;
import org.jboss.windup.reporting.service.ReportService;
import org.jboss.windup.util.exception.WindupException;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Creates the main report HTML page for a Java application.
 */
@RuleMetadata(phase = ReportGenerationPhase.class)
public class CreateUnparsableFilesReportRuleProvider extends AbstractRuleProvider {
    public static final String REPORT_NAME = "Unparsable";
    public static final String TEMPLATE_UNPARSABLE = "/reports/templates/unparsable_files.ftl";
    public static final String DESCRIPTION = "This report shows all files that could not been parsed in the expected format. For instance, a file with a '.xml' or '.wsdl' suffix is assumed to be an XML file. If the XML parser fails on it, you'll see that here. Besides that, the information about parsing failure is also present wherever the individual file is listed.";

    // @formatter:off
    @Override
    public Configuration getConfiguration(RuleLoaderContext ruleLoaderContext) {
        // Create the ReportModel.
        GraphOperation createReportModel = new GraphOperation() {
            @Override
            public void perform(GraphRewrite event, EvaluationContext context) {
                WindupConfigurationModel windupConfiguration = WindupConfigurationService.getConfigurationModel(event.getGraphContext());
                for (FileModel fileModel : windupConfiguration.getInputPaths()) {
                    ProjectModel application = fileModel.getProjectModel();
                    if (application == null)
                        throw new WindupException("Error, no project found in: " + fileModel.getFilePath());

                    createReportModel(event.getGraphContext(), application);
                }
            }

            public String toString() {
                return "addReport";
            }
        };

        // For each FileModel...
        return ConfigurationBuilder.begin()
                .addRule()
                .perform(createReportModel);

    }
    // @formatter:on

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

    private void createReportModel(GraphContext context, ProjectModel application) {
        ProjectModelTraversal traversal = new ProjectModelTraversal(application);
        List<ProjectModel> projects = getProjectsWithUnparsableFiles(traversal);

        if (projects.isEmpty())
            return;

        GraphService<UnparsablesAppReportModel> service = new GraphService<>(context, UnparsablesAppReportModel.class);
        UnparsablesAppReportModel reportModel = service.create();
        reportModel.setReportPriority(120);
        reportModel.setDisplayInApplicationReportIndex(true);
        reportModel.setReportName(REPORT_NAME);
        reportModel.setDescription(DESCRIPTION);
        reportModel.setReportIconClass("glyphicon glyphicon-warning-sign");
        reportModel.setMainApplicationReport(false);
        reportModel.setProjectModel(application);
        reportModel.setTemplatePath(TEMPLATE_UNPARSABLE);
        reportModel.setTemplateType(TemplateType.FREEMARKER);

        reportModel.setAllSubProjects(projects);

        // Set the filename for the report
        ReportService reportService = new ReportService(context);
        reportService.setUniqueFilename(reportModel, REPORT_NAME + "_" + application.getName(), "html");
    }
}
