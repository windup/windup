package org.jboss.windup.config.ruleprovider;

import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.RuleProvider;
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.config.metadata.RuleProviderMetadata;
import org.ocpsoft.rewrite.config.Condition;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.config.Operation;
import org.ocpsoft.rewrite.config.Rule;
import org.ocpsoft.rewrite.context.EvaluationContext;
import org.ocpsoft.rewrite.event.Rewrite;

/**
 * A {@link RuleProvider} that provides only a single {@link Rule} (itself).
 *
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public abstract class SingleRuleProvider extends AbstractRuleProvider implements Rule {
    public SingleRuleProvider() {
        super();
    }

    public SingleRuleProvider(RuleProviderMetadata metadata) {
        super(metadata);
    }

    public SingleRuleProvider(Class<? extends RuleProvider> implementationType, String id) {
        super(implementationType, id);
    }

    @Override
    public String getId() {
        return getMetadata().getID();
    }

    @Override
    public final Configuration getConfiguration(RuleLoaderContext ruleLoaderContext) {
        return ConfigurationBuilder.begin()
                .addRule()
                .when(this)
                .perform(this);
    }

    /**
     * Evaluate this {@link Rule} against the given {@link GraphRewrite} event. If this {@link Condition} does not apply
     * to the given event, it must return <code>false</code>. If the condition applies and is satisfied, return
     * <code>true</code>. (Default <code>true</code>.)
     */
    public boolean evaluate(GraphRewrite event, EvaluationContext context) {
        return true;
    }

    /**
     * Perform the {@link Operation}.
     */
    public abstract void perform(GraphRewrite event, EvaluationContext context);

    @Override
    public final boolean evaluate(Rewrite event, EvaluationContext context) {
        if (event instanceof GraphRewrite)
            return evaluate((GraphRewrite) event, context);
        return false;
    }

    @Override
    public final void perform(Rewrite event, EvaluationContext context) {
        if (event instanceof GraphRewrite)
            perform((GraphRewrite) event, context);
    }
}
