package org.jboss.windup.reporting.rules.rendering;

import javax.inject.Inject;

import org.jboss.forge.furnace.Furnace;
import org.jboss.windup.config.RulePhase;
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.reporting.freemarker.FreeMarkerIterationOperation;
import org.jboss.windup.reporting.model.ApplicationReportModel;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;

/**
 * This renders the ApplicationReport, along with all of its subapplications via freemarker.
 * 
 * @author jsightler <jesse.sightler@gmail.com>
 * 
 */
public class RenderApplicationReportRuleProvider extends WindupRuleProvider
{
    @Inject
    private Furnace furnace;

    @Override
    public RulePhase getPhase()
    {
        return RulePhase.REPORT_RENDERING;
    }

    // @formatter:off
    @Override
    public Configuration getConfiguration(GraphContext context)
    {
        FreeMarkerIterationOperation reportOperation = FreeMarkerIterationOperation.create(furnace);

        return ConfigurationBuilder
            .begin()
            .addRule()
            .when(Query.find(ApplicationReportModel.class))
            .perform(reportOperation);
    }
    // @formatter:on
}
