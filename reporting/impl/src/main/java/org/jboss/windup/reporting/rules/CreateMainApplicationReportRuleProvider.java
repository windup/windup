package org.jboss.windup.reporting.rules;

import javax.inject.Inject;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.RulePhase;
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.config.operation.ruleelement.AbstractIterationOperation;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.reporting.model.ApplicationReportModel;
import org.jboss.windup.reporting.model.MainNavigationIndexModel;
import org.jboss.windup.reporting.service.MainNavigationIndexModelService;
import org.jboss.windup.reporting.service.ReportModelService;
import org.jboss.windup.util.exception.WindupException;
import org.ocpsoft.rewrite.config.ConditionBuilder;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

public class CreateMainApplicationReportRuleProvider extends WindupRuleProvider
{

    @Inject
    private ReportModelService reportModelService;

    @Inject
    private MainNavigationIndexModelService mainNavigationIndexService;

    @Override
    public RulePhase getPhase()
    {
        return RulePhase.REPORT_GENERATION;
    }

    // @formatter:off
    @Override
    public Configuration getConfiguration(GraphContext context)
    {
        ConditionBuilder findProjectModels = Query
                    .find(WindupConfigurationModel.class);

        AbstractIterationOperation<WindupConfigurationModel> addApplicationReport = new AbstractIterationOperation<WindupConfigurationModel>(
                    WindupConfigurationModel.class)
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
                    .when(findProjectModels)
                    .perform(
                                Iteration.over()
                                            .perform(addApplicationReport).endIteration()
                    );

    }
    // @formatter:on

    private ApplicationReportModel createApplicationReport(GraphContext context, ProjectModel projectModel)
    {
        ApplicationReportModel applicationReportModel = context.getFramed().addVertex(null,
                    ApplicationReportModel.class);
        applicationReportModel.setReportPriority(100);
        applicationReportModel.setReportName("Application");
        applicationReportModel.setProjectModel(projectModel);

        // Create the index, and add this report to it
        MainNavigationIndexModel navIndex = mainNavigationIndexService.create();
        applicationReportModel.setMainNavigationIndexModel(navIndex);
        navIndex.addReportModel(applicationReportModel);
        addAllProjectModels(navIndex, projectModel);

        // Set the filename for the report
        reportModelService.setUniqueFilename(applicationReportModel, projectModel.getName(), "html");

        return applicationReportModel;
    }

    private void addAllProjectModels(MainNavigationIndexModel navIdx, ProjectModel projectModel)
    {
        navIdx.addProjectModel(projectModel);
        for (ProjectModel childProject : projectModel.getChildProjects())
        {
            addAllProjectModels(navIdx, childProject);
        }
    }
}
