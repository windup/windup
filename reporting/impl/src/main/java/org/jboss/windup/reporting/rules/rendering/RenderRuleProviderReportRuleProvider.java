package org.jboss.windup.reporting.rules.rendering;

import javax.inject.Inject;

import org.jboss.forge.furnace.Furnace;
import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.metadata.MetadataBuilder;
import org.jboss.windup.config.phase.PostFinalizePhase;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.reporting.freemarker.FreeMarkerOperation;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.config.Rule;

/**
 * Renders a report of all {@link AbstractRuleProvider}s that were loaded by Windup, as well as the results of each
 * {@link Rule} execution.
 * 
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 *
 */
public class RenderRuleProviderReportRuleProvider extends AbstractRuleProvider
{
    private static final String OUTPUT_FILENAME = "windup_ruleproviders.html";
    private static final String TEMPLATE = "/reports/templates/ruleprovidersummary.ftl";

    @Inject
    private Furnace furnace;

    public RenderRuleProviderReportRuleProvider()
    {
        super(MetadataBuilder.forProvider(RenderRuleProviderReportRuleProvider.class)
                    .setPhase(PostFinalizePhase.class)
                    .addExecuteAfter(ExecutionTimeReportRuleProvider.class));
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
