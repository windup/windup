package org.jboss.windup.config.metadata;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.phase.DependentPhase;
import org.jboss.windup.config.ruleprovider.SingleRuleProvider;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * @author <a href="mailto:ozizka@redhat.com">Ondrej Zizka</a>
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public class MetadataTestRuleProvider1 extends SingleRuleProvider {
    public MetadataTestRuleProvider1() {
        super(MetadataBuilder.forProvider(MetadataTestRuleProvider1.class).setPhase(DependentPhase.class));
    }

    @Override
    public void perform(GraphRewrite event, EvaluationContext context) {
        ExecutedProviders.executedProvider(this);
    }

}