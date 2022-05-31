package org.jboss.windup.reporting.rules.rendering;

import org.jboss.forge.furnace.Furnace;
import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.phase.PostFinalizePhase;
import org.jboss.windup.reporting.freemarker.FreeMarkerOperation;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.config.Rule;

import javax.inject.Inject;

/**
 * Renders a report of all {@link AbstractRuleProvider}s that were loaded by Windup, as well as the results of each
 * {@link Rule} execution.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
@RuleMetadata(phase = PostFinalizePhase.class, after = ExecutionTimeReportRuleProvider.class)
public class RenderRuleProviderReportRuleProvider extends AbstractRuleProvider {
    public static final String OUTPUT_FILENAME = "windup_ruleproviders.html";
    private static final String TEMPLATE = "/reports/templates/ruleprovidersummary.ftl";

    @Inject
    private Furnace furnace;

    @Override
    public Configuration getConfiguration(RuleLoaderContext ruleLoaderContext) {
        FreeMarkerOperation generateReportOperation =
                FreeMarkerOperation.create(furnace, TEMPLATE, OUTPUT_FILENAME);
        return ConfigurationBuilder.begin()
                .addRule()
                .perform(generateReportOperation);
    }
}
