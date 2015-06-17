package org.jboss.windup.rules.apps.tattletale;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.metadata.MetadataBuilder;
import org.jboss.windup.config.operation.GraphOperation;
import org.jboss.windup.config.phase.ReportGenerationPhase;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.WindupConfigurationService;
import org.jboss.windup.reporting.model.ApplicationReportModel;
import org.jboss.windup.reporting.model.TemplateType;
import org.jboss.windup.reporting.service.ApplicationReportService;
import org.jboss.windup.reporting.service.ReportService;
import org.jboss.windup.util.Logging;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

public class CreateTattletaleReportLinkRuleProvider extends AbstractRuleProvider
{
    private static final Logger LOG = Logging.get(CreateTattletaleReportLinkRuleProvider.class);
    
    public static final String REPORT_TEMPLATE = "/reports/templates/embedded.ftl";
    private static final String TATTLETALE_INDEX_HTML = "tattletale/index.html";

    public CreateTattletaleReportLinkRuleProvider()
    {
        super(MetadataBuilder.forProvider(CreateTattletaleReportLinkRuleProvider.class)
                    .setPhase(ReportGenerationPhase.class));
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
            applicationReportModel.setReportIconClass("glyphicon tattletale-nav-logo");
            applicationReportModel.setReportPriority(500);
            
            applicationReportModel.setDisplayInApplicationReportIndex(true);
            applicationReportModel.setProjectModel(inputProjectModel);
            applicationReportModel.setReportFilename(TATTLETALE_INDEX_HTML);
            applicationReportModel.setTemplatePath(REPORT_TEMPLATE);
            applicationReportModel.setTemplateType(TemplateType.FREEMARKER);

            
            Map<String, String> reportProperties = new HashMap<>();
            reportProperties.put("embeddedTitle", "Tattletale Report");
            reportProperties.put("embeddedUrl", "tattletale/index.html");
            
            applicationReportModel.setReportProperties(reportProperties);
            
            
            LOG.info("Title: "+applicationReportModel.getReportProperties().get("embeddedTitle"));
            LOG.info("URL: "+applicationReportModel.getReportProperties().get("embeddedUrl"));
            
            ReportService reportService = new ReportService(event.getGraphContext());
            reportService.setUniqueFilename(applicationReportModel, "tattletale"+"_"+inputProjectModel.getName(), "html");
        }
    }
}
