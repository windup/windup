package org.jboss.windup.rules.apps.javaee.rules;

import java.util.logging.Logger;

import org.jboss.windup.rules.apps.javaee.model.stats.ProjectTechnologiesStatsService;
import org.jboss.windup.util.Logging;
import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.operation.GraphOperation;
import org.jboss.windup.config.phase.PreReportGenerationPhase;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * Creates the technologies statistics for the Technologies Report.
 *
 * @author <a href="mailto:zizka@seznam.cz">Ondrej Zizka</a>
 */
@RuleMetadata(phase = PreReportGenerationPhase.class, perform = "Compute the statistics for the Technologies Report")
public class TechnologiesStatsRuleProvider extends AbstractRuleProvider
{
    @Override
    public Configuration getConfiguration(RuleLoaderContext context) {
        return ConfigurationBuilder.begin().addRule().perform(
            new GraphOperation() {
                @Override
                public void perform(GraphRewrite event, EvaluationContext context) {
                    ProjectTechnologiesStatsService statsService = new ProjectTechnologiesStatsService(event.getGraphContext());
                    statsService.computeStats();
                }
            }
        ).withId(getClass().getSimpleName() + "_computeStats");
    }
}
