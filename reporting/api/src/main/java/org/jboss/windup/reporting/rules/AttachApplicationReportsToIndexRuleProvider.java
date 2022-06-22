package org.jboss.windup.reporting.rules;

import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.operation.iteration.AbstractIterationOperation;
import org.jboss.windup.config.phase.PostReportGenerationPhase;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.reporting.model.ApplicationReportIndexModel;
import org.jboss.windup.reporting.model.ApplicationReportModel;
import org.jboss.windup.reporting.service.ApplicationReportIndexService;
import org.ocpsoft.rewrite.config.ConditionBuilder;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * Attaches {@link ApplicationReportModel}s to the {@link ApplicationReportIndexModel}
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
@RuleMetadata(phase = PostReportGenerationPhase.class)
public class AttachApplicationReportsToIndexRuleProvider extends AbstractRuleProvider {
    @Override
    public Configuration getConfiguration(RuleLoaderContext ruleLoaderContext) {
        ConditionBuilder applicationReportFound = Query.fromType(ApplicationReportModel.class);

        AbstractIterationOperation<ApplicationReportModel> addToApplicationIndex = new AddToApplicationIndex();

        return ConfigurationBuilder.begin()
                .addRule()
                .when(applicationReportFound)
                .perform(addToApplicationIndex);
    }

    private class AddToApplicationIndex extends AbstractIterationOperation<ApplicationReportModel> {
        @Override
        public void perform(GraphRewrite event, EvaluationContext context, ApplicationReportModel payload) {
            final ApplicationReportIndexService applicationReportIndexService = new ApplicationReportIndexService(event.getGraphContext());
            final ProjectModel projectModel = payload.getProjectModel();

            if (projectModel == null || Boolean.TRUE == payload.getDisplayInGlobalApplicationIndex()) {
                ApplicationReportIndexModel index = applicationReportIndexService.getOrCreateGlobalApplicationIndex();
                index.addApplicationReportModel(payload);
            }

            if (projectModel != null) {
                ApplicationReportIndexModel index = applicationReportIndexService
                        .getApplicationReportIndexForProjectModel(payload.getProjectModel());
                index.addApplicationReportModel(payload);
            }
        }

        @Override
        public String toString() {
            return "AddToApplicationIndex";
        }
    }
}
