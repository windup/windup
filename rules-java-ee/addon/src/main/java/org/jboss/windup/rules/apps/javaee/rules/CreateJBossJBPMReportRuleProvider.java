package org.jboss.windup.rules.apps.javaee.rules;

import java.util.HashMap;
import java.util.Map;

import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.metadata.MetadataBuilder;
import org.jboss.windup.config.operation.GraphOperation;
import org.jboss.windup.config.phase.ReportGenerationPhase;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.graph.service.WindupConfigurationService;
import org.jboss.windup.reporting.model.ApplicationReportModel;
import org.jboss.windup.reporting.model.TemplateType;
import org.jboss.windup.reporting.model.WindupVertexListModel;
import org.jboss.windup.reporting.service.ApplicationReportService;
import org.jboss.windup.reporting.service.ReportService;
import org.jboss.windup.rules.apps.javaee.model.Jbpm3ProcessModel;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * Creates a report of Jbpm 3 Process Image Report
 *
 */
public class CreateJBossJBPMReportRuleProvider extends AbstractRuleProvider
{
    public static final String TEMPLATE_EJB_REPORT = "/reports/templates/jbpm.ftl";

    public CreateJBossJBPMReportRuleProvider()
    {
        super(MetadataBuilder.forProvider(CreateJBossJBPMReportRuleProvider.class, "Create JBPM Report")
                    .setPhase(ReportGenerationPhase.class));
    }

    @Override
    public Configuration getConfiguration(GraphContext context)
    {
        return ConfigurationBuilder.begin()
                    .addRule()
                    .when(Query.fromType(Jbpm3ProcessModel.class))
                    .perform(new GraphOperation()
                    {
                        @Override
                        public void perform(GraphRewrite event, EvaluationContext context)
                        {
                            // configuration of current execution
                            WindupConfigurationModel configurationModel = WindupConfigurationService.getConfigurationModel(event.getGraphContext());

                            // reference to input project model
                            ProjectModel projectModel = configurationModel.getInputPath().getProjectModel();
                            createJbpmReport(event.getGraphContext(), projectModel);
                        }

                        @Override
                        public String toString()
                        {
                            return "CreateJBPM3Report";
                        }
                    });

    }

    private void createJbpmReport(GraphContext context, ProjectModel projectModel)
    {
        ApplicationReportService applicationReportService = new ApplicationReportService(context);
        ApplicationReportModel applicationReportModel = applicationReportService.create();
        applicationReportModel.setReportPriority(300);
        applicationReportModel.setDisplayInApplicationReportIndex(true);
        applicationReportModel.setReportName("JBPM Report");
        applicationReportModel.setReportIconClass("glyphicon bpm-nav-logo");
        applicationReportModel.setProjectModel(projectModel);
        applicationReportModel.setTemplatePath(TEMPLATE_EJB_REPORT);
        applicationReportModel.setTemplateType(TemplateType.FREEMARKER);

        GraphService<Jbpm3ProcessModel> jbpmProcessService = new GraphService<>(context, Jbpm3ProcessModel.class);
        GraphService<WindupVertexListModel> listService = new GraphService<WindupVertexListModel>(context, WindupVertexListModel.class);

        WindupVertexListModel processes = listService.create();

        for (Jbpm3ProcessModel processModel : jbpmProcessService.findAll())
        {
            processes.addItem(processModel);
        }

        Map<String, WindupVertexFrame> additionalData = new HashMap<>(4);
        additionalData.put("processes", processes);
        applicationReportModel.setRelatedResource(additionalData);

        ReportService reportService = new ReportService(context);
        reportService.setUniqueFilename(applicationReportModel, "jbpmreport_" + projectModel.getName(), "html");
    }
}
