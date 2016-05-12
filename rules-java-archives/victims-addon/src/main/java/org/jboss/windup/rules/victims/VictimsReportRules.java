package org.jboss.windup.rules.victims;

import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.operation.iteration.AbstractIterationOperation;
import org.jboss.windup.config.phase.ReportGenerationPhase;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.graph.service.WindupConfigurationService;
import org.jboss.windup.rules.victims.model.AffectedJarModel;
import org.jboss.windup.rules.victims.model.VictimsReportModel;
import org.jboss.windup.reporting.model.TemplateType;
import org.jboss.windup.reporting.service.ReportService;
import org.jboss.windup.rules.apps.java.model.WindupJavaConfigurationModel;
import org.ocpsoft.rewrite.config.ConditionBuilder;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;


/**
 * Creates a report for all archives identified by the Victims service as containing a security vulnerability.
 *
 * @author Ondrej Zizka
 */
@RuleMetadata(tags = { "java" }, phase = ReportGenerationPhase.class)
public class VictimsReportRules extends AbstractRuleProvider
{
    public static final String TITLE = "Security";
    public static final String TEMPLATE_REPORT = "/org/jboss/windup/rules/victims/Report-Security.ftl.html";

    // @formatter:off
    @Override
    public Configuration getConfiguration(GraphContext context)
    {
        ConditionBuilder applicationProjectModelsFound = Query.fromType(WindupJavaConfigurationModel.class);

        AbstractIterationOperation<WindupJavaConfigurationModel> addApplicationReport = new AbstractIterationOperation<WindupJavaConfigurationModel>()
        {
            @Override
            public void perform(GraphRewrite event, EvaluationContext context, WindupJavaConfigurationModel payload)
            {
                WindupConfigurationModel configurationModel = WindupConfigurationService.getConfigurationModel(event.getGraphContext());
                for (FileModel inputPath : configurationModel.getInputPaths())
                {
                    ProjectModel projectModel = inputPath.getProjectModel();
                    createReport(event.getGraphContext(), payload, projectModel);
                }
            }

            @Override
            public String toString()
            {
                return "VictimsReportRules";
            }
        };

        return ConfigurationBuilder.begin()
            .addRule()
            .when(applicationProjectModelsFound)
            .perform(addApplicationReport);
    }
    // @formatter:on


    private VictimsReportModel createReport(GraphContext graphCtx,
                WindupJavaConfigurationModel javaCfg, ProjectModel rootProjectModel)
    {
        GraphService<VictimsReportModel> reportServ = new GraphService<>(graphCtx, VictimsReportModel.class);
        VictimsReportModel reportM = reportServ.create();

        // Report metadata
        reportM.setReportPriority(1000);
        reportM.setReportName(TITLE);
        reportM.setMainApplicationReport(false);
        reportM.setDisplayInApplicationReportIndex(true);
        reportM.setProjectModel(rootProjectModel);
        reportM.setTemplatePath(TEMPLATE_REPORT);
        reportM.setTemplateType(TemplateType.FREEMARKER);
        reportM.setReportIconClass("glyphicon glyphicon-fire");
        reportM.setDescription(
            "A list of the security issues found in the application, such like archives containing security vulnerabilities."
            + "Disclaimer: This list may not contain all vulnerabilities. Even if it's empty, your application still may contain security flaws.");

        // Get all jars
        GraphService<AffectedJarModel> jarService = new GraphService<>(graphCtx, AffectedJarModel.class);
        Iterable<AffectedJarModel> jars = jarService.findAll();

        // For each affected jar...
        for (AffectedJarModel jar : jars)
        {
            reportM.addAffectedJar(jar);
        }

        // Set the filename for the report
        ReportService reportService = new ReportService(graphCtx);
        reportService.setUniqueFilename(reportM, "vulnerableJars", "html");
        return reportM;
    }

}
