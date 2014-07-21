package org.jboss.windup.reporting.rules;

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
import org.jboss.windup.util.exception.WindupException;
import org.ocpsoft.rewrite.config.ConditionBuilder;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

public class CreateApplicationReportRuleProvider extends WindupRuleProvider
{

    private static final String CONFIGURATION_MODEL = "windupCfg";
    private static final String CONFIGURATION_MODELS = "windupCfgs";

    @Override
    public RulePhase getPhase()
    {
        return RulePhase.REPORT_GENERATION;
    }

    @Override
    public Configuration getConfiguration(GraphContext context)
    {
        ConditionBuilder findProjectModels = Query
                    .find(WindupConfigurationModel.class)
                    .as(CONFIGURATION_MODELS);

        AbstractIterationOperation<WindupConfigurationModel> addApplicationReport = new AbstractIterationOperation<WindupConfigurationModel>(
                    WindupConfigurationModel.class, CONFIGURATION_MODEL)
        {
            @Override
            public void perform(GraphRewrite event, EvaluationContext context, WindupConfigurationModel payload)
            {
                ProjectModel projectModel = payload.getInputPath().getProjectModel();
                if (projectModel == null)
                {
                    if (payload.isSourceMode())
                    {
                        throw new WindupException("Error, no project found in source-based input directory: "
                                    + payload.getInputPath().getFilePath());
                    }
                    else
                    {
                        throw new WindupException("Error, no project found in archive: "
                                    + payload.getInputPath().getFilePath());
                    }
                }
                createApplicationReport(event.getGraphContext(), projectModel);
            }
        };

        return ConfigurationBuilder
                    .begin()
                    .addRule()
                    .when(findProjectModels)
                    .perform(
                                Iteration.over(CONFIGURATION_MODELS).as(CONFIGURATION_MODEL)
                                            .perform(addApplicationReport).endIteration()
                    );

    }

    private ApplicationReportModel createApplicationReport(GraphContext context, ProjectModel projectModel)
    {
        ApplicationReportModel applicationReportModel = context.getFramed().addVertex(null,
                    ApplicationReportModel.class);
        applicationReportModel.setApplicationName(projectModel.getRootFileModel().getPrettyPath());
        applicationReportModel.setReportName(projectModel.getName());
        applicationReportModel.addProjectModel(projectModel);

        addSubModels(applicationReportModel, projectModel);

        return applicationReportModel;
    }

    private void addSubModels(ApplicationReportModel reportModel, ProjectModel projectModel)
    {
        for (ProjectModel subModel : projectModel.getChildProjects())
        {
            reportModel.addProjectModel(subModel);
            addSubModels(reportModel, subModel);
        }
    }
}
