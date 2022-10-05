package org.jboss.windup.reporting.rules;

import org.jboss.forge.furnace.util.Iterators;
import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.operation.iteration.AbstractIterationOperation;
import org.jboss.windup.config.phase.PreReportGenerationPhase;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.reporting.model.ApplicationReportIndexModel;
import org.jboss.windup.reporting.service.ApplicationReportIndexService;
import org.jboss.windup.util.exception.WindupException;
import org.ocpsoft.rewrite.config.ConditionBuilder;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * For each input path, creates an index that can be used to register reports related to each application on that input.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
@RuleMetadata(phase = PreReportGenerationPhase.class)
public class CreateApplicationReportIndexRuleProvider extends AbstractRuleProvider {
    @Override
    public Configuration getConfiguration(RuleLoaderContext ruleLoaderContext) {
        ConditionBuilder applicationsFound = Query.fromType(WindupConfigurationModel.class);

        AbstractIterationOperation<WindupConfigurationModel> addApplicationReportIndex = new AbstractIterationOperation<WindupConfigurationModel>() {
            @Override
            public void perform(GraphRewrite event, EvaluationContext context, WindupConfigurationModel payload) {
                for (FileModel inputPath : payload.getInputPaths()) {
                    ProjectModel projectModel = inputPath.getProjectModel();
                    if (projectModel == null) {
                        throw new WindupException("Error, no project found in: " + inputPath.getFilePath());
                    }
                    createApplicationReportIndex(event.getGraphContext(), projectModel);
                }
            }

            @Override
            public String toString() {
                return "AddApplicationReportIndex";
            }
        };

        return ConfigurationBuilder.begin()
                .addRule()
                .when(applicationsFound)
                .perform(addApplicationReportIndex);

    }

    /**
     * Create the index and associate it with all project models in the Application
     */
    private ApplicationReportIndexModel createApplicationReportIndex(GraphContext context,
                                                                     ProjectModel applicationProjectModel) {
        ApplicationReportIndexService applicationReportIndexService = new ApplicationReportIndexService(context);
        ApplicationReportIndexModel index = applicationReportIndexService.create();

        addAllProjectModels(index, applicationProjectModel);

        return index;
    }

    /**
     * Attach all project models within the application to the index. This will make it easy to navigate from the
     * projectModel to the application index.
     */
    private void addAllProjectModels(ApplicationReportIndexModel navIdx, ProjectModel projectModel) {
        navIdx.addProjectModel(projectModel);
        for (ProjectModel childProject : projectModel.getChildProjects()) {
            if (!Iterators.asSet(navIdx.getProjectModels()).contains(childProject))
                addAllProjectModels(navIdx, childProject);
        }
    }

}
