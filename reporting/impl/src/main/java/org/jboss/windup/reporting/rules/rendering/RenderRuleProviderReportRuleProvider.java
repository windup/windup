package org.jboss.windup.reporting.rules.rendering;

import java.util.List;

import javax.inject.Inject;

import org.jboss.forge.furnace.Furnace;
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.config.phase.PostFinalize;
import org.jboss.windup.config.phase.RulePhase;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.reporting.freemarker.FreeMarkerOperation;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;

/**
 * Renders a report of all {@link WindupRuleProvider}s that were loaded by Windup, as well as the results of each {@link Rule} execution.
 * 
 * @author jsightler <jesse.sightler@gmail.com>
 *
 */
public class RenderRuleProviderReportRuleProvider extends WindupRuleProvider
{
    private static final String OUTPUT_FILENAME = "windup_ruleproviders.html";
    private static final String TEMPLATE = "/reports/templates/ruleprovidersummary.ftl";

    @Inject
    private Furnace furnace;

    @Override
    public Class<? extends RulePhase> getPhase()
    {
        return PostFinalize.class;
    }

    @Override
    public List<Class<? extends WindupRuleProvider>> getExecuteAfter()
    {
        return asClassList(ExecutionTimeReportRuleProvider.class);
    }

    @Override
    public Configuration getConfiguration(GraphContext context)
    {
        FreeMarkerOperation generateReportOperation =
                    FreeMarkerOperation.create(furnace, TEMPLATE, OUTPUT_FILENAME);
        return ConfigurationBuilder.begin()
                    .addRule()
                    .perform(generateReportOperation);
    }
}
