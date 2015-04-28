package org.jboss.windup.rules.apps.java.reporting.rules;

import java.util.HashMap;
import java.util.Map;

import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.metadata.MetadataBuilder;
import org.jboss.windup.config.operation.iteration.AbstractIterationOperation;
import org.jboss.windup.config.phase.ReportGenerationPhase;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.reporting.model.AboutWindupModel;
import org.jboss.windup.reporting.model.ApplicationReportModel;
import org.jboss.windup.reporting.model.TemplateType;
import org.jboss.windup.reporting.service.ApplicationReportService;
import org.jboss.windup.reporting.service.ReportService;
import org.jboss.windup.util.exception.WindupException;
import org.ocpsoft.rewrite.config.ConditionBuilder;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

public class CreateAboutWindupReportRuleProvider extends AbstractRuleProvider
{
    public static final String REPORT_NAME = "About";
    public static final String TEMPLATE_APPLICATION_REPORT = "/reports/templates/about_windup.ftl";

    public CreateAboutWindupReportRuleProvider()
    {
        super(MetadataBuilder.forProvider(CreateAboutWindupReportRuleProvider.class)
                    .setPhase(ReportGenerationPhase.class));
    }

    // @formatter:off
    @Override
    public Configuration getConfiguration(GraphContext context)
    {
        ConditionBuilder applicationProjectModelsFound = Query
                    .fromType(WindupConfigurationModel.class);

        AbstractIterationOperation<WindupConfigurationModel> addApplicationReport = new AbstractIterationOperation<WindupConfigurationModel>()
        {
            @Override
            public void perform(GraphRewrite event, EvaluationContext context, WindupConfigurationModel payload)
            {
                ProjectModel projectModel = payload.getInputPath().getProjectModel();
                if (projectModel == null)
                {
                    throw new WindupException("Error, no project found in: " + payload.getInputPath().getFilePath());
                }
                createApplicationReport(event.getGraphContext(), projectModel);
            }
            
            @Override
            public String toString()
            {
                return "CreateAboutWindupReport";
            }
        };

        return ConfigurationBuilder.begin()
                    .addRule()
                    .when(applicationProjectModelsFound)
                    .perform(addApplicationReport);

    }
    // @formatter:on

    private ApplicationReportModel createApplicationReport(GraphContext context, ProjectModel projectModel)
    {
    	ApplicationReportService applicationReportService = new ApplicationReportService(context);
    	ApplicationReportModel applicationReportModel = applicationReportService.create();
    	
    	applicationReportModel.setReportPriority(10000);
        applicationReportModel.setDisplayInApplicationReportIndex(true);
        applicationReportModel.setReportName(REPORT_NAME);
        applicationReportModel.setReportIconClass("glyphicon glyphicon-info-sign");
        applicationReportModel.setMainApplicationReport(true);
        applicationReportModel.setProjectModel(projectModel);
        applicationReportModel.setTemplatePath(TEMPLATE_APPLICATION_REPORT);
        applicationReportModel.setTemplateType(TemplateType.FREEMARKER);

        Map<String, WindupVertexFrame> related = new HashMap<String, WindupVertexFrame>();
        AboutWindupModel aboutWindupModel = context.getFramed().addVertex(null, AboutWindupModel.class);
        
        //TODO: replace with the utility call that @lincolnthree is going to be adding to the utility package.
        aboutWindupModel.setWindupRuntimeVersion("2.3.0.SNAPSHOT");
        related.put("windupAbout", aboutWindupModel);
        
        applicationReportModel.setRelatedResource(related);
        
        // Set the filename for the report
        ReportService reportService = new ReportService(context);
        reportService.setUniqueFilename(applicationReportModel, "about_"+projectModel.getName(), "html");

        return applicationReportModel;
    }
    
}


