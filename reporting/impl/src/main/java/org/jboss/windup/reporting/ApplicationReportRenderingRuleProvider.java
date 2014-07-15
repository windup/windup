package org.jboss.windup.reporting;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.RulePhase;
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.config.operation.ruleelement.AbstractIterationOperation;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.reporting.freemarker.FreeMarkerIterationOperation;
import org.jboss.windup.reporting.meta.ApplicationReportModel;
import org.jboss.windup.reporting.meta.TemplateType;
import org.jboss.windup.util.FilenameUtil;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

public class ApplicationReportRenderingRuleProvider extends WindupRuleProvider
{
    private static final String APP_REPORTS_VAR = "applicationReportsIterable";
    private static final String APP_REPORT_VAR = "applicationReport";
    private static final String TEMPLATE_APPLICATION_REPORT = "/reports/templates/application.ftl";

    @Override
    public RulePhase getPhase()
    {
        return RulePhase.REPORT_RENDERING;
    }

    @Override
    public Configuration getConfiguration(GraphContext context)
    {
        AbstractIterationOperation<ApplicationReportModel> setupTemplateOperation = new AbstractIterationOperation<ApplicationReportModel>(
                    ApplicationReportModel.class, APP_REPORT_VAR)
        {
            @Override
            public void perform(GraphRewrite event, EvaluationContext context, ApplicationReportModel payload)
            {
                payload.setTemplatePath(TEMPLATE_APPLICATION_REPORT);
                payload.setTemplateType(TemplateType.FREEMARKER);
                String applicationname = payload.getApplicationName();
                String filename = FilenameUtil.cleanFileName(applicationname) + ".html";

                String outputDir = GraphService.getConfigurationModel(event.getGraphContext()).getOutputPath()
                            .getFilePath();
                Path outputPath = Paths.get(outputDir, filename);
                for (int i = 1; Files.exists(outputPath); i++)
                {
                    filename = FilenameUtil.cleanFileName(applicationname) + "." + i + ".html";
                    outputPath = Paths.get(outputDir, filename);
                }

                payload.setReportFilename(filename);
            }
        };

        FreeMarkerIterationOperation reportOperation = FreeMarkerIterationOperation.create(APP_REPORT_VAR);

        return ConfigurationBuilder
                    .begin()
                    .addRule()
                    .when(Query.find(ApplicationReportModel.class).as(APP_REPORTS_VAR))
                    .perform(Iteration.over(APP_REPORTS_VAR).as(APP_REPORT_VAR)
                                .perform(setupTemplateOperation.and(reportOperation))
                                .endIteration());
    }
}
