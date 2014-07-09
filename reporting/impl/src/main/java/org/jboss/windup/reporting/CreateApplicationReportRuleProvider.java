package org.jboss.windup.reporting;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.RulePhase;
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.config.operation.ruleelement.AbstractIterationOperation;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ApplicationModel;
import org.jboss.windup.reporting.meta.ApplicationReportModel;
import org.ocpsoft.rewrite.config.ConditionBuilder;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

public class CreateApplicationReportRuleProvider extends WindupRuleProvider
{

    private static final String VAR_APPLICATION_MODEL = "applicationModel";
    private static final String VAR_APPLICATION_MODELS = "applicationModels";

    @Override
    public RulePhase getPhase()
    {
        return RulePhase.REPORT_GENERATION;
    }

    @Override
    public Configuration getConfiguration(GraphContext context)
    {
        ConditionBuilder findApplicationModels = Query
                    .find(ApplicationModel.class)
                    .as(VAR_APPLICATION_MODELS);

        AbstractIterationOperation<ApplicationModel> addApplicationReport = new AbstractIterationOperation<ApplicationModel>(
                    ApplicationModel.class, VAR_APPLICATION_MODEL)
        {
            @Override
            public void perform(GraphRewrite event, EvaluationContext context, ApplicationModel payload)
            {
                ApplicationReportModel applicationReportModel = event.getGraphContext()
                            .getFramed()
                            .addVertex(null, ApplicationReportModel.class);
                applicationReportModel.setApplicationName(payload.getApplicationName());
                applicationReportModel.setReportName(payload.getApplicationName());
            }
        };

        return ConfigurationBuilder
                    .begin()
                    .addRule()
                    .when(findApplicationModels)
                    .perform(
                                Iteration.over(VAR_APPLICATION_MODELS).as(VAR_APPLICATION_MODEL)
                                            .perform(addApplicationReport).endIteration()
                    );

    }
}
