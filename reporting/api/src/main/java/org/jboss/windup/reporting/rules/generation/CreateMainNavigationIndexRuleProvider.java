package org.jboss.windup.reporting.rules.generation;

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
import org.jboss.windup.reporting.model.MainNavigationIndexModel;
import org.jboss.windup.reporting.service.MainNavigationIndexModelService;
import org.jboss.windup.util.exception.WindupException;
import org.ocpsoft.rewrite.config.ConditionBuilder;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * Creates the main application index. Anything that needs to link to the application index should depend on this.
 * 
 * @author jsightler
 * 
 */
public class CreateMainNavigationIndexRuleProvider extends WindupRuleProvider
{

    @Inject
    private MainNavigationIndexModelService mainNavigationIndexService;

    @Override
    public RulePhase getPhase()
    {
        return RulePhase.REPORT_GENERATION;
    }

    @Override
    public Configuration getConfiguration(GraphContext context)
    {
        ConditionBuilder findProjectModels = Query
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
                createMainNavigationIndex(event.getGraphContext(), projectModel);
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

    /**
     * Create the index and associate it with all project models
     */
    private MainNavigationIndexModel createMainNavigationIndex(GraphContext context, ProjectModel projectModel)
    {
        // Create the index, and add this report to it
        MainNavigationIndexModel navIndex = mainNavigationIndexService.create();
        addAllProjectModels(navIndex, projectModel);

        return navIndex;
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
