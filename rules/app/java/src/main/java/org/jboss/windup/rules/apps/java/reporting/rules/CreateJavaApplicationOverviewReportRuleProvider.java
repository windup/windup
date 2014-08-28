package org.jboss.windup.rules.apps.java.reporting.rules;

import javax.inject.Inject;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.RulePhase;
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.config.operation.ruleelement.AbstractIterationOperation;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.reporting.model.ApplicationReportModel;
import org.jboss.windup.reporting.service.ReportModelService;
import org.jboss.windup.util.exception.WindupException;
import org.ocpsoft.rewrite.config.ConditionBuilder;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

public class CreateJavaApplicationOverviewReportRuleProvider extends WindupRuleProvider
{

    @Inject
    private ReportModelService reportModelService;

    @Override
    public RulePhase getPhase()
    {
        return RulePhase.REPORT_GENERATION;
    }

    // @formatter:off
    @Override
    public Configuration getConfiguration(GraphContext context)
    {
        ConditionBuilder applicationProjectModelsFound = Query
                    .find(WindupConfigurationModel.class);

        AbstractIterationOperation<WindupConfigurationModel> addApplicationReport = new AbstractIterationOperation<WindupConfigurationModel>()
        {
            @Override
            public void perform(GraphRewrite event, EvaluationContext context, WindupConfigurationModel payload)
            {
                ProjectModel projectModel = payload.getInputPath().getProjectModel();
                if (projectModel == null)
                {
                    String msg = payload.isSourceMode() ? "source-based input directory" : "archive";
                    throw new WindupException("Error, no project found in " + msg + ": "
                                + payload.getInputPath().getFilePath());
                }
                createApplicationReport(event.getGraphContext(), projectModel);
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
        ApplicationReportModel applicationReportModel = context.getFramed().addVertex(null,
                    ApplicationReportModel.class);
        applicationReportModel.setReportPriority(100);
        applicationReportModel.setDisplayInApplicationReportIndex(true);
        applicationReportModel.setReportName("Overview");
        applicationReportModel.setProjectModel(projectModel);

        // Set the filename for the report
        reportModelService.setUniqueFilename(applicationReportModel, projectModel.getName(), "html");

        return applicationReportModel;
    }
}
