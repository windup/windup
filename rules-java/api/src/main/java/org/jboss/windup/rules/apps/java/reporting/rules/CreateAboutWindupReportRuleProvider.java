package org.jboss.windup.rules.apps.java.reporting.rules;

import org.jboss.forge.furnace.addons.Addon;
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
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.reporting.model.AboutWindupModel;
import org.jboss.windup.reporting.model.ApplicationReportModel;
import org.jboss.windup.reporting.model.TemplateType;
import org.jboss.windup.reporting.service.ApplicationReportService;
import org.jboss.windup.reporting.service.ReportService;
import org.jboss.windup.util.ThemeProvider;
import org.jboss.windup.util.exception.WindupException;
import org.ocpsoft.rewrite.config.ConditionBuilder;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

/**
 * Create a report HTML page about Windup.
 */
@RuleMetadata(phase = ReportGenerationPhase.class)
public class CreateAboutWindupReportRuleProvider extends AbstractRuleProvider {
    public static final String REPORT_DESCRIPTION = "This describes the version of " + ThemeProvider.getInstance().getTheme().getBrandNameLong() + " used to generate this report and provides helpful links for further assistance.";
    public static final String REPORT_NAME = "About";
    public static final String TEMPLATE_APPLICATION_REPORT = "/reports/templates/about_windup.ftl";

    @Inject
    Addon addon;

    // @formatter:off
    @Override
    public Configuration getConfiguration(RuleLoaderContext ruleLoaderContext) {
        ConditionBuilder windupConfigurationFound = Query.fromType(WindupConfigurationModel.class);

        AbstractIterationOperation<WindupConfigurationModel> addApplicationReport = new AbstractIterationOperation<WindupConfigurationModel>() {
            @Override
            public void perform(GraphRewrite event, EvaluationContext context, WindupConfigurationModel payload) {
                createAboutWindup(event.getGraphContext(), null);

                for (FileModel inputPath : payload.getInputPaths()) {
                    ProjectModel projectModel = inputPath.getProjectModel();
                    if (projectModel == null) {
                        throw new WindupException("Error, no project found in: " + inputPath.getFilePath());
                    }
                    createAboutWindup(event.getGraphContext(), projectModel);
                }
            }

            @Override
            public String toString() {
                return "CreateAboutWindupReport";
            }
        };

        return ConfigurationBuilder.begin()
                .addRule()
                .when(windupConfigurationFound)
                .perform(addApplicationReport);
    }
    // @formatter:on

    private ApplicationReportModel createAboutWindup(GraphContext context, ProjectModel projectModel) {
        ApplicationReportService applicationReportService = new ApplicationReportService(context);
        ApplicationReportModel applicationReportModel = applicationReportService.create();

        applicationReportModel.setReportPriority(10000);
        applicationReportModel.setReportName(REPORT_NAME);
        applicationReportModel.setDescription(REPORT_DESCRIPTION);
        applicationReportModel.setReportIconClass("fa fa-question-circle");
        applicationReportModel.setMainApplicationReport(false);
        applicationReportModel.setDisplayInApplicationReportIndex(true);

        if (projectModel == null)
            applicationReportModel.setDisplayInGlobalApplicationIndex(true);
        else
            applicationReportModel.setProjectModel(projectModel);

        applicationReportModel.setTemplatePath(TEMPLATE_APPLICATION_REPORT);
        applicationReportModel.setTemplateType(TemplateType.FREEMARKER);

        Map<String, WindupVertexFrame> related = new HashMap<>();
        AboutWindupModel aboutWindupModel = context.getFramed().addFramedVertex(AboutWindupModel.class);

        aboutWindupModel.setWindupRuntimeVersion(addon.getId().getVersion().toString());
        related.put("windupAbout", aboutWindupModel);

        applicationReportModel.setRelatedResource(related);

        // Set the filename for the report
        ReportService reportService = new ReportService(context);
        String filename = projectModel == null ? "about_global" : "about_" + projectModel.getName();
        reportService.setUniqueFilename(applicationReportModel, filename, "html");

        return applicationReportModel;
    }

}
