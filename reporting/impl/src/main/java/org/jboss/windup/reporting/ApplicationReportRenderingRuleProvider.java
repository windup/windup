package org.jboss.windup.reporting;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.RulePhase;
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.config.graphsearch.GraphSearchConditionBuilder;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.config.operation.ruleelement.AbstractIterationOperator;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.reporting.meta.ApplicationReportModel;
import org.jboss.windup.reporting.meta.TemplateType;
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
        AbstractIterationOperator<ApplicationReportModel> setupTemplateOperation = new AbstractIterationOperator<ApplicationReportModel>(
                    ApplicationReportModel.class, APP_REPORT_VAR)
        {
            @Override
            public void perform(GraphRewrite event, EvaluationContext context, ApplicationReportModel payload)
            {
                payload.setTemplatePath(TEMPLATE_APPLICATION_REPORT);
                payload.setTemplateType(TemplateType.FREEMARKER);
                payload.setReportFilename(payload.getApplicationName() + ".html");
            }
        };

        FreeMarkerIterationOperation reportOperation = FreeMarkerIterationOperation.create(APP_REPORT_VAR);

        return ConfigurationBuilder
                    .begin()
                    .addRule()
                    .when(GraphSearchConditionBuilder.create(APP_REPORTS_VAR).ofType(ApplicationReportModel.class))
                    .perform(Iteration.over(APP_REPORTS_VAR).var(APP_REPORT_VAR)
                                .perform(setupTemplateOperation.and(reportOperation))
                                .endIteration());
    }
}
