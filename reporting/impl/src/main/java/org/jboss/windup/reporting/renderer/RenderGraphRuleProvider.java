package org.jboss.windup.reporting.renderer;

import javax.inject.Inject;

import org.jboss.forge.furnace.services.Imported;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.RulePhase;
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.config.operation.GraphOperation;
import org.jboss.windup.graph.GraphContext;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * Renders the graph using each existing GraphRenderer.
 */
public class RenderGraphRuleProvider extends WindupRuleProvider
{
    @Inject
    private Imported<GraphRenderer> renderers;

    @Override
    public RulePhase getPhase()
    {
        return RulePhase.POST_REPORT_RENDERING;
    }

    @Override
    public Configuration getConfiguration(GraphContext arg0)
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
                        renderer.renderGraph();
                    }
                }
            });
    }
}
