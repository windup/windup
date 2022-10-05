package org.jboss.windup.config.ruleprovider;

import org.jboss.forge.furnace.util.Annotations;
import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.RuleProvider;
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.metadata.RuleProviderMetadata;
import org.jboss.windup.config.operation.iteration.AbstractIterationOperation;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.ocpsoft.rewrite.config.ConditionBuilder;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * This provides a simplified way to extend {@link AbstractRuleProvider} for cases where the rule simply needs to
 * provide some query, and wants to execute a function over each resulting row.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public abstract class IteratingRuleProvider<PAYLOADTYPE extends WindupVertexFrame> extends AbstractRuleProvider {

    public IteratingRuleProvider() {
        super();
    }

    public IteratingRuleProvider(RuleProviderMetadata metadata) {
        super(metadata);
    }

    public IteratingRuleProvider(Class<? extends RuleProvider> implementationType, String id) {
        super(implementationType, id);
    }

    /**
     * Gets the condition for the {@link Configuration}'s "when" clause.
     */
    public abstract ConditionBuilder when();

    /**
     * Perform this function for each {@link WindupVertexFrame} returned by the "when" clause.
     */
    public abstract void perform(GraphRewrite event, EvaluationContext context, PAYLOADTYPE payload);

    private class IterationOperation extends AbstractIterationOperation<PAYLOADTYPE> {
        @Override
        public void perform(GraphRewrite event, EvaluationContext context, PAYLOADTYPE payload) {
            IteratingRuleProvider.this.perform(event, context, payload);
        }

        @Override
        public String toString() {
            return IteratingRuleProvider.this.toStringPerform();
        }
    }

    /**
     * This should return a string describing the operation to be performed by the subclass.
     */
    public String toStringPerform() {
        if (Annotations.isAnnotationPresent(getClass(), RuleMetadata.class)) {
            RuleMetadata metadata = Annotations.getAnnotation(getClass(), RuleMetadata.class);
            if (!"".equals(metadata.perform()))
                return metadata.perform();
        }
        throw new IllegalStateException(getClass().getName() +
                " must either override 'toStringPerform()', or specify @" + RuleMetadata.class.getName() + "(perform = \"...\").");
    }

    @Override
    public final Configuration getConfiguration(RuleLoaderContext ruleLoaderContext) {
        return ConfigurationBuilder.begin()
                .addRule()
                .when(when())
                .perform(new IterationOperation());
    }
}
