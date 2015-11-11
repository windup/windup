package org.jboss.windup.rules.apps.javaee.rules;

import java.util.HashMap;
import java.util.Map;

import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.metadata.MetadataBuilder;
import org.jboss.windup.config.operation.GraphOperation;
import org.jboss.windup.config.phase.ReportGenerationPhase;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.model.resource.FileModel;
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
import org.jboss.windup.rules.apps.javaee.service.EjbBeanService;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * Creates a report of EJB data (eg, a list of EJB session beans).
 *
 */
public class CreateEJBReportRuleProvider extends AbstractRuleProvider
{
    public static final String TEMPLATE_EJB_REPORT = "/reports/templates/ejb.ftl";

    public CreateEJBReportRuleProvider()
    {
        super(MetadataBuilder.forProvider(CreateEJBReportRuleProvider.class, "Create EJB Report")
                    .setPhase(ReportGenerationPhase.class));
    }

    @Override
    public Configuration getConfiguration(GraphContext context)
    {
        return ConfigurationBuilder.begin()
                    .addRule()
                    .perform(new GraphOperation()
                    {
                        @Override
                        public void perform(GraphRewrite event, EvaluationContext context)
                        {
                            // configuration of current execution
                            WindupConfigurationModel configurationModel = WindupConfigurationService.getConfigurationModel(event.getGraphContext());

                            for (FileModel inputPath : configurationModel.getInputPaths())
                            {
                                ProjectModel projectModel = inputPath.getBoundProject();
                                createEJBReport(event.getGraphContext(), projectModel);
                            }
                        }

                        @Override
                        public String toString()
                        {
                            return "CreateEJBReport";
                        }
                    });

    }

    @SuppressWarnings("unchecked")
    private void createEJBReport(GraphContext context, ProjectModel projectModel)
    {
        EjbBeanService ejbService = new EjbBeanService(context);
        GraphService<WindupVertexListModel> listService = new GraphService<>(context, WindupVertexListModel.class);

        WindupVertexListModel<EjbBeanBaseModel> entityList = listService.create();
        WindupVertexListModel<EjbBeanBaseModel> mdbList = listService.create();
        WindupVertexListModel<EjbBeanBaseModel> statelessList = listService.create();
        WindupVertexListModel<EjbBeanBaseModel> statefulList = listService.create();

        boolean itemAdded = false;
        for (EjbBeanBaseModel ejbModel : ejbService.findAll())
        {
            if (!ejbModel.getApplication().equals(projectModel))
                continue;

            itemAdded = true;

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

        if (!itemAdded)
            return;

        ApplicationReportService applicationReportService = new ApplicationReportService(context);
        ApplicationReportModel applicationReportModel = applicationReportService.create();
        applicationReportModel.setReportPriority(300);
        applicationReportModel.setDisplayInApplicationReportIndex(true);
        applicationReportModel.setReportName("EJBs");
        applicationReportModel.setReportIconClass("glyphicon ejb-nav-logo");
        applicationReportModel.setProjectModel(projectModel);
        applicationReportModel.setTemplatePath(TEMPLATE_EJB_REPORT);
        applicationReportModel.setTemplateType(TemplateType.FREEMARKER);

        Map<String, WindupVertexFrame> data = new HashMap<>(4);
        data.put("entity", entityList);
        data.put("mdb", mdbList);
        data.put("stateless", statelessList);
        data.put("stateful", statefulList);
        applicationReportModel.setRelatedResource(data);

        ReportService reportService = new ReportService(context);
        reportService.setUniqueFilename(applicationReportModel, "ejbreport_" + projectModel.getName(), "html");
    }
}
