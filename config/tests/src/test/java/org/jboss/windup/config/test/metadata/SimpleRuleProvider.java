package org.jboss.windup.config.test.metadata;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.config.condition.GraphCondition;
import org.jboss.windup.config.operation.GraphOperation;
import org.jboss.windup.graph.GraphContext;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.config.ConfigurationRuleBuilderCustom;
import org.ocpsoft.rewrite.config.ConfigurationRuleBuilderPerform;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 *
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public abstract class SimpleRuleProvider extends WindupRuleProvider {

    private final GraphCondition cond;
    private final GraphOperation op;


    public SimpleRuleProvider(GraphCondition cond, GraphOperation op)
    {
        super();
        this.cond = cond;
        this.op = op;
    }

    public SimpleRuleProvider(GraphCondition cond)
    {
        this(cond, null);
    }


    public SimpleRuleProvider(GraphOperation op)
    {
        this(null, op);
    }



    // @formatter:off
    @Override
    public Configuration getConfiguration(GraphContext context)
    {
        ConfigurationRuleBuilderCustom rule = ConfigurationBuilder.begin().addRule();

        if(this.cond != null)
            rule.when(this.cond);

        ConfigurationRuleBuilderPerform perform = null;

        if(this.op != null)
            perform = rule.perform(
                new GraphOperation()
                {
                    public void perform(GraphRewrite event, EvaluationContext evCtx)
                    {
                        SimpleRuleProvider.this.op.perform(event, evCtx);
                    }
                }
            );

        return perform;
    }
    // @formatter:on

}// class
