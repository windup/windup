package org.jboss.windup.config.metadata;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.config.operation.GraphOperation;
import org.jboss.windup.graph.GraphContext;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 *
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public abstract class TestInsiderRuleProvider extends WindupRuleProvider implements Insider {


    // @formatter:off
    @Override
    public Configuration getConfiguration(GraphContext context)
    {
        return ConfigurationBuilder.begin()
        .addRule()
        .perform(
            new GraphOperation()
            {
                public void perform(GraphRewrite event, EvaluationContext evCtx)
                {
                    call(event, evCtx);
                }
            }
        );
    }
    // @formatter:on

}// class
