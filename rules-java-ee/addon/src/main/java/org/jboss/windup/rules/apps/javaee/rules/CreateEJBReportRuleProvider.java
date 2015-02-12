package org.jboss.windup.rules.apps.javaee.rules;

import java.util.HashMap;
import java.util.Map;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.config.operation.GraphOperation;
import org.jboss.windup.config.phase.ReportGeneration;
import org.jboss.windup.config.phase.RulePhase;
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
import org.jboss.windup.rules.apps.javaee.model.EjbBeanBaseModel;
import org.jboss.windup.rules.apps.javaee.model.EjbEntityBeanModel;
import org.jboss.windup.rules.apps.javaee.model.EjbMessageDrivenModel;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * Creates a report of EJB data (eg, a list of EJB session beans).
 *
 */
public class CreateEJBReportRuleProvider extends WindupRuleProvider
{
    public static final String TEMPLATE_EJB_REPORT = "/reports/templates/ejb.ftl";

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
                    .when(Query.fromType(EjbBeanBaseModel.class))
                    .perform(new GraphOperation()
                    {
                        @Override
                        public void perform(GraphRewrite event, EvaluationContext context)
                        {
                            // configuration of current execution
                            WindupConfigurationModel configurationModel = WindupConfigurationService.getConfigurationModel(event.getGraphContext());

                            // reference to input project model
                            ProjectModel projectModel = configurationModel.getInputPath().getProjectModel();
                            createEJBReport(event.getGraphContext(), projectModel);
                        }

                        @Override
                        public String toString()
                        {
                            return "CreateEJBReport";
                        }
                    });

    }

    private void createEJBReport(GraphContext context, ProjectModel projectModel)
    {
        ApplicationReportService applicationReportService = new ApplicationReportService(context);
        ApplicationReportModel applicationReportModel = applicationReportService.create();
        applicationReportModel.setReportPriority(300);
        applicationReportModel.setDisplayInApplicationReportIndex(true);
        applicationReportModel.setReportName("EJB Report");
        applicationReportModel.setReportIconClass("glyphicon ejb-nav-logo");
        applicationReportModel.setProjectModel(projectModel);
        applicationReportModel.setTemplatePath(TEMPLATE_EJB_REPORT);
        applicationReportModel.setTemplateType(TemplateType.FREEMARKER);

        GraphService<EjbBeanBaseModel> ejbService = new GraphService<EjbBeanBaseModel>(context, EjbBeanBaseModel.class);
        GraphService<WindupVertexListModel> listService = new GraphService<WindupVertexListModel>(context, WindupVertexListModel.class);

        WindupVertexListModel entityList = listService.create();
        WindupVertexListModel mdbList = listService.create();
        WindupVertexListModel statelessList = listService.create();
        WindupVertexListModel statefulList = listService.create();

        for (EjbBeanBaseModel ejbModel : ejbService.findAll())
        {
            if (ejbModel instanceof EjbMessageDrivenModel)
            {
                mdbList.addItem(ejbModel);
            }
            else if (ejbModel instanceof EjbEntityBeanModel)
            {
                entityList.addItem(ejbModel);
            }
            else
            {
                if ("stateful".equalsIgnoreCase(ejbModel.getSessionType()))
                {
                    statefulList.addItem(ejbModel);
                }
                else
                {
                    statelessList.addItem(ejbModel);
                }
            }
        }
        Map<String, WindupVertexFrame> additionalData = new HashMap<>(4);
        additionalData.put("entity", entityList);
        additionalData.put("mdb", mdbList);
        additionalData.put("stateless", statelessList);
        additionalData.put("stateful", statefulList);
        applicationReportModel.setRelatedResource(additionalData);

        // Set the filename for the report
        ReportService reportService = new ReportService(context);
        reportService.setUniqueFilename(applicationReportModel, "ejbreport_" + projectModel.getName(), "html");
    }
}
