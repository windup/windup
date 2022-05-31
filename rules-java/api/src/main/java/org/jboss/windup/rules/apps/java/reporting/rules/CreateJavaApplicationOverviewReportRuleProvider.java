package org.jboss.windup.rules.apps.java.reporting.rules;

import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.operation.iteration.AbstractIterationOperation;
import org.jboss.windup.config.phase.ReportGenerationPhase;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.graph.traversal.ProjectModelTraversal;
import org.jboss.windup.reporting.model.OverviewReportLineMessageModel;
import org.jboss.windup.reporting.model.TaggableModel;
import org.jboss.windup.reporting.model.TemplateType;
import org.jboss.windup.reporting.service.ReportService;
import org.jboss.windup.rules.apps.java.model.JavaApplicationOverviewReportModel;
import org.jboss.windup.util.exception.WindupException;
import org.ocpsoft.rewrite.config.ConditionBuilder;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Creates the main report HTML page for a Java application.
 */
@RuleMetadata(phase = ReportGenerationPhase.class)
public class CreateJavaApplicationOverviewReportRuleProvider extends AbstractRuleProvider {
    public static final String DETAILS_REPORT = "Application Details";
    public static final String TEMPLATE_APPLICATION_REPORT = "/reports/templates/java_application.ftl";
    public static final String DESCRIPTION = "An exhaustive list of all of the information and issues found within the application.";

    // @formatter:off
    @Override
    public Configuration getConfiguration(RuleLoaderContext ruleLoaderContext) {
        ConditionBuilder applicationProjectModelsFound = Query
                .fromType(WindupConfigurationModel.class);

        AbstractIterationOperation<WindupConfigurationModel> addApplicationReport = new AbstractIterationOperation<WindupConfigurationModel>() {
            @Override
            public void perform(GraphRewrite event, EvaluationContext context, WindupConfigurationModel payload) {
                for (FileModel inputPath : payload.getInputPaths()) {
                    ProjectModel application = inputPath.getProjectModel();
                    if (application == null) {
                        throw new WindupException("Error, no project found in: " + inputPath.getFilePath());
                    }
                    createReport(event.getGraphContext(), application);
                }
            }

            @Override
            public String toString() {
                return "CreateJavaApplicationOverviewReport";
            }
        };

        return ConfigurationBuilder.begin()
                .addRule()
                .when(applicationProjectModelsFound)
                .perform(addApplicationReport);

    }
    // @formatter:on

    private void createReport(GraphContext context, ProjectModel application) {
        GraphService<JavaApplicationOverviewReportModel> service = new GraphService<>(context, JavaApplicationOverviewReportModel.class);
        JavaApplicationOverviewReportModel applicationReportModel = service.create();
        applicationReportModel.setReportPriority(102);
        applicationReportModel.setDisplayInApplicationReportIndex(true);
        applicationReportModel.setReportName(DETAILS_REPORT);
        applicationReportModel.setDescription(DESCRIPTION);
        applicationReportModel.setReportIconClass("glyphicon glyphicon-th-list");
        applicationReportModel.setMainApplicationReport(false);
        applicationReportModel.setProjectModel(application);
        applicationReportModel.setTemplatePath(TEMPLATE_APPLICATION_REPORT);
        applicationReportModel.setTemplateType(TemplateType.FREEMARKER);
        applicationReportModel.setIncludeTags(Collections.emptySet());
        applicationReportModel.setExcludeTags(Collections.singleton(TaggableModel.CATCHALL_TAG));
        GraphService<OverviewReportLineMessageModel> lineNotesService = new GraphService<>(context, OverviewReportLineMessageModel.class);
        Iterable<OverviewReportLineMessageModel> allLines = lineNotesService.findAll();
        Set<String> dupeCheck = new HashSet<>();
        ProjectModelTraversal projectModelTraversal = new ProjectModelTraversal(application);
        Set<ProjectModel> allProjectsInApplication = projectModelTraversal.getAllProjects(true);

        for (OverviewReportLineMessageModel line : allLines) {
            if (dupeCheck.contains(line.getMessage()))
                continue;

            ProjectModel project = line.getProject();
            if (allProjectsInApplication.contains(project)) {
                dupeCheck.add(line.getMessage());
                applicationReportModel.addApplicationReportLine(line);
            }
        }
        // Set the filename for the report
        ReportService reportService = new ReportService(context);
        reportService.setUniqueFilename(applicationReportModel, "ApplicationDetails_" + application.getName(), "html");
    }
}
