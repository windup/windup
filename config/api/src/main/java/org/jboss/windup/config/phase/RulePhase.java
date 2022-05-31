package org.jboss.windup.config.phase;

import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.RuleProvider;
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.config.metadata.MetadataBuilder;
import org.jboss.windup.config.metadata.RuleProviderMetadata;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.config.Rule;

/**
 * Provides a shorthand for stating the order of execution of {@link Rule}s within Windup. When a {@link AbstractRuleProvider} specifies a
 * {@link RulePhase}, the results of calling {@link RulePhase#getExecuteAfter()} and {@link RulePhase#getExecuteBefore()} on the rule will appended to
 * the results of {@link RuleProviderMetadata#getExecuteAfter()} and {@link RulePhase#getExecuteBefore()}.
 * <p>
 * In this way, most {@link AbstractRuleProvider}s will be able to simply specify a {@link RulePhase} in order to determine their approximate
 * placement within the execution lifecycle.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public abstract class RulePhase extends AbstractRuleProvider {
    private final Class<? extends RulePhase> thisPhase;
    private Class<? extends RulePhase> previousPhase;
    private Class<? extends RulePhase> nextPhase;

    public RulePhase(Class<? extends RulePhase> implementationType) {
        super(implementationType, implementationType.getSimpleName());
        this.thisPhase = implementationType;
    }

    /**
     * This will always return an empty {@link Configuration} as these are just barriers that do not provide additional
     * {@link Rule}s.
     */
    @Override
    public final Configuration getConfiguration(RuleLoaderContext ruleLoaderContext) {
        return ConfigurationBuilder.begin();
    }

    @Override
    public final RuleProviderMetadata getMetadata() {
        return MetadataBuilder.forProvider(thisPhase, thisPhase.getSimpleName())

                /**
                 * We set {@link DependentPhase} because it has no executeBefore/executeAfter conditions of its own.
                 * The phase should provide it's own dependencies and not a recursive call to "getPhase()", so
                 * returning a value that has no dependencies of its own is necessary here.
                 */
                .setPhase(DependentPhase.class)
                .addExecuteAfter(_getExecuteAfter())
                .addExecuteBefore(_getExecuteBefore());
    }

    private Class<? extends RuleProvider> _getExecuteBefore() {
        return nextPhase == null ? getExecuteBefore() : nextPhase;
    }

    private Class<? extends RuleProvider> _getExecuteAfter() {
        return previousPhase == null ? getExecuteAfter() : previousPhase;
    }

    /**
     * This allows the {@link RulePhase} implementation to specify the {@link RulePhase} implementation that it should
     * be executed after.
     */
    public abstract Class<? extends RulePhase> getExecuteAfter();

    /**
     * This allows the sorter to link {@link RulePhase}s to each other, without them having to explicitly specify both
     * the executeAfter and executeBefore.
     */
    public final void setExecuteAfter(Class<? extends RulePhase> previousPhase) {
        this.previousPhase = previousPhase;
    }

    /**
     * This allows the {@link RulePhase} implementation to specify the {@link RulePhase} implementation that it should
     * be executed before.
     */
    public abstract Class<? extends RulePhase> getExecuteBefore();

    /**
     * This allows the sorter to link {@link RulePhase}s to each other, without them having to explicitly specify both
     * the executeAfter and executeBefore.
     */
    public final void setExecuteBefore(Class<? extends RulePhase> nextPhase) {
        this.nextPhase = nextPhase;
    }
}
