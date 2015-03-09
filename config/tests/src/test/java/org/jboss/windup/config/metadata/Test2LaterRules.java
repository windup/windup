package org.jboss.windup.config.metadata;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.phase.DependentPhase;
import org.jboss.windup.config.ruleprovider.SingleRuleProvider;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 *
 * @author Ondrej Zizka, ozizka at redhat.com
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public class Test2LaterRules extends SingleRuleProvider
{
    public Test2LaterRules()
    {
        super(MetadataBuilder.forProvider(Test2LaterRules.class).setPhase(DependentPhase.class));
    }

    @Override
    public void perform(GraphRewrite event, EvaluationContext context)
    {
        MetadataTestExecutedProviders.executedProvider(this);
    }
}