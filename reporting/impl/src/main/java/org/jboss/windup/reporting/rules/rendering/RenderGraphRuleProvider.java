package org.jboss.windup.reporting.rules.rendering;

import org.jboss.forge.furnace.services.Imported;
import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.operation.GraphOperation;
import org.jboss.windup.config.phase.PostReportRenderingPhase;
import org.jboss.windup.reporting.renderer.GraphRenderer;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

import javax.enterprise.inject.Vetoed;
import javax.inject.Inject;

/**
 * This renders the graph itself to the output directory for debug purposes.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
@Vetoed
@RuleMetadata(phase = PostReportRenderingPhase.class)
public class RenderGraphRuleProvider extends AbstractRuleProvider {
    @Inject
    private Imported<GraphRenderer> renderers;

    @Override
    public Configuration getConfiguration(final RuleLoaderContext ruleLoaderContext) {
        return ConfigurationBuilder.begin()
                .addRule()
                .perform(new GraphOperation() {
                    @Override
                    public void perform(GraphRewrite event, EvaluationContext context) {
                        for (GraphRenderer renderer : renderers) {
                            renderer.renderGraph(event.getGraphContext());
                        }
                    }

                    @Override
                    public String toString() {
                        return "RenderGraphForDebugging";
                    }
                });
    }
}
