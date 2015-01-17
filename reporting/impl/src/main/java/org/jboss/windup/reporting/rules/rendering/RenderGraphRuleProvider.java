package org.jboss.windup.reporting.rules.rendering;

import javax.enterprise.inject.Vetoed;
import javax.inject.Inject;

import org.jboss.forge.furnace.services.Imported;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.config.operation.GraphOperation;
import org.jboss.windup.config.phase.PostReportRendering;
import org.jboss.windup.config.phase.RulePhase;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.reporting.renderer.GraphRenderer;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * 
 * This renders the graph itself to the output directory for debug purposes.
 * 
 * @author jsightler <jesse.sightler@gmail.com>
 * 
 */
@Vetoed
public class RenderGraphRuleProvider extends WindupRuleProvider
{
    @Inject
    private Imported<GraphRenderer> renderers;

    @Override
    public Class<? extends RulePhase> getPhase()
    {
        return PostReportRendering.class;
    }

    @Override
    public Configuration getConfiguration(final GraphContext graphContext)
    {
        return ConfigurationBuilder.begin()
                    .addRule()
                    .perform(new GraphOperation()
                    {
                        @Override
                        public void perform(GraphRewrite event, EvaluationContext context)
                        {
                            for (GraphRenderer renderer : renderers)
                            {
                                renderer.renderGraph(graphContext);
                            }
                        }

                        @Override
                        public String toString()
                        {
                            return "RenderGraphForDebugging";
                        }
                    });
    }
}
