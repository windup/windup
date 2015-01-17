package org.jboss.windup.rules.apps.tattletale;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.config.operation.GraphOperation;
import org.jboss.windup.config.phase.ReportGeneration;
import org.jboss.windup.config.phase.RulePhase;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.WindupConfigurationService;
import org.jboss.windup.reporting.model.ApplicationReportModel;
import org.jboss.windup.reporting.model.TemplateType;
import org.jboss.windup.reporting.service.ApplicationReportService;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

public class CreateTattletaleReportLinkRuleProvider extends WindupRuleProvider
{
    private static final String TATTLETALE_INDEX_HTML = "tattletale/index.html";

    @Override
    public Class<? extends RulePhase> getPhase()
    {
        return ReportGeneration.class;
    }

    @Override
    public Configuration getConfiguration(GraphContext context)
    {
        return ConfigurationBuilder.begin()
                    .addRule()
                    .perform(new CreateTattletaleReportModelOperation());
    }

    private class CreateTattletaleReportModelOperation extends GraphOperation
    {

        @Override
        public void perform(GraphRewrite event, EvaluationContext context)
        {
            WindupConfigurationModel cfg = WindupConfigurationService.getConfigurationModel(event.getGraphContext());
            FileModel inputFM = cfg.getInputPath();
            ProjectModel inputProjectModel = inputFM.getProjectModel();

            ApplicationReportModel applicationReportModel = new ApplicationReportService(event.getGraphContext()).create();
            applicationReportModel.setReportName("Tattletale");
            applicationReportModel.setDisplayInApplicationReportIndex(true);
            applicationReportModel.setProjectModel(inputProjectModel);
            applicationReportModel.setTemplateType(TemplateType.OTHER);
            applicationReportModel.setReportFilename(TATTLETALE_INDEX_HTML);
        }
    }
}
