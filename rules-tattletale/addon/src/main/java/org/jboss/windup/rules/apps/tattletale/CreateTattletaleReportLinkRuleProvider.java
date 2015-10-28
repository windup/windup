package org.jboss.windup.rules.apps.tattletale;

import java.util.logging.Logger;

import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.metadata.MetadataBuilder;
import org.jboss.windup.config.operation.GraphOperation;
import org.jboss.windup.config.phase.ReportGenerationPhase;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.WindupConfigurationService;
import org.jboss.windup.util.Logging;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * Adds a link to the Tattletale report to the applicationReportModel.
 */
public class CreateTattletaleReportLinkRuleProvider extends AbstractRuleProvider
{
    private static final Logger LOG = Logging.get(CreateTattletaleReportLinkRuleProvider.class);



    public CreateTattletaleReportLinkRuleProvider()
    {
        super(MetadataBuilder.forProvider(CreateTattletaleReportLinkRuleProvider.class)
                    .setPhase(ReportGenerationPhase.class));
    }

    @Override
    public Configuration getConfiguration(GraphContext context)
    {
        return ConfigurationBuilder.begin()
                    .addRule()
                    .perform(new CreateTattletaleReportModelOperation());
    }

    private class CreateTattletaleReportModelOperation extends GraphOperation
    {

        @Override
        public void perform(GraphRewrite event, EvaluationContext context)
        {
            WindupConfigurationModel configuration = WindupConfigurationService.getConfigurationModel(event.getGraphContext());
            for (FileModel input : configuration.getInputPaths())
            {

            }
        }
    }
}
