package org.jboss.windup.rules.apps.java.ip;

import java.util.ArrayList;
import java.util.List;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.config.operation.GraphOperation;
import org.jboss.windup.config.phase.MigrationRules;
import org.jboss.windup.config.phase.RulePhase;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.graph.service.WindupConfigurationService;
import org.jboss.windup.reporting.model.ApplicationReportModel;
import org.jboss.windup.reporting.model.TemplateType;
import org.jboss.windup.reporting.service.ReportService;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * Finds files that contain potential static IP addresses, determined by regular
 * expression.
 * 
 * @author <a href="mailto:bradsdavis@gmail.com">Brad Davis</a>
 */
public class CreateDiscoverStaticIPAddressReportRuleProvider extends WindupRuleProvider {
	private static final String TITLE = "Static IP Addresses";
	private static final String TEMPLATE_REPORT = "/reports/templates/static_ip_addresses.ftl";
	
	
	@Override
	public Class<? extends RulePhase> getPhase() {
		return MigrationRules.class;
	}

	@Override
	public Configuration getConfiguration(final GraphContext graphContext) {

		return ConfigurationBuilder
				.begin()
				.addRule()
				//when a IP Location Model exists...
				.when(Query.fromType(StaticIPLocationModel.class))
				//perform the write of this report once (GraphOperation)...
				.perform(new GraphOperation() {
					
					@Override
					public void perform(GraphRewrite event, EvaluationContext context) {
						//configuration of current execution
						WindupConfigurationModel configurationModel = WindupConfigurationService.getConfigurationModel(event.getGraphContext());
						
						//reference to input project model
						ProjectModel projectModel = configurationModel.getInputPath().getProjectModel();
						createIPReport(event.getGraphContext(), projectModel);
					}
				});
	}

	
	private StaticIPLocationReportModel createIPReport(GraphContext context,  ProjectModel rootProjectModel){
		GraphService<StaticIPLocationReportModel> graphReportService = new GraphService<StaticIPLocationReportModel>(context, StaticIPLocationReportModel.class);
		
		//create a reference in the graph to the static ip location report.
		StaticIPLocationReportModel applicationReport = graphReportService.create();
		
		applicationReport.setReportPriority(600);
		applicationReport.setReportName(TITLE);
		applicationReport.setTemplatePath(TEMPLATE_REPORT);
		applicationReport.setDisplayInApplicationReportIndex(true);
		applicationReport.setTemplateType(TemplateType.FREEMARKER);
        applicationReport.setDisplayInApplicationList(false);
        applicationReport.setMainApplicationReport(false);
        applicationReport.setProjectModel(rootProjectModel);
        
        //find all IPLocationModels
        GraphService<StaticIPLocationModel> ipLocationModelService = new GraphService<StaticIPLocationModel>(context, StaticIPLocationModel.class);
        Iterable<StaticIPLocationModel> results = ipLocationModelService.findAll();
        
        for(StaticIPLocationModel location : results) {
        	applicationReport.addStaticIPLocation(location);
        }
        
        //performs methods on the graph to create a unique file name.
        ReportService reportService = new ReportService(context);
        
        //uses project model's name for the report name.
        reportService.setUniqueFilename(applicationReport, "static_ips" + rootProjectModel.getName(), "html");
        return applicationReport;
	}
}
