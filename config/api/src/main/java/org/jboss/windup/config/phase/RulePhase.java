package org.jboss.windup.config.phase;

import java.util.List;

import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.graph.GraphContext;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.config.Rule;

/**
 * Provides a shorthand for stating the order of execution of {@link Rule}s within Windup. When a {@link WindupRuleProvider} specifies a
 * {@link RulePhase}, the results of calling {@link RulePhase#getExecuteAfter()} and {@link RulePhase#getExecuteBefore()} on the rule will appended to
 * the results of {@link WindupRuleProvider#getExecuteAfter()} and {@link RulePhase#getExecuteBefore()}.
 * 
 * In this way, most {@link WindupRuleProvider}s will be able to simply specify a {@link RulePhase} in order to determine their approximate placement
 * within the execution lifecycle.
 * 
 * @author jsightler <jesse.sightler@gmail.com>
 */
public abstract class RulePhase extends WindupRuleProvider
{
    private Class<? extends RulePhase> previousPhase;
    private Class<? extends RulePhase> nextPhase;

    /**
     * This will always return an empty {@link Configuration} as these are just barriers that do not provide additional {@link Rule}s.
     */
    @Override
    public final Configuration getConfiguration(GraphContext context)
    {
        return ConfigurationBuilder.begin();
    }

    /**
     * This returns {@link Implicit} as that phase has no executeBefore/executeAfter conditions of its own. The phase should provide it's own
     * dependencies and not a recursive "getPhase()" so returning a value that has no dependencies of its own is necessary here.
     */
    @Override
    public final Class<? extends RulePhase> getPhase()
    {
        return Implicit.class;
    }

    /**
     * Provides a simplified id for this rule.
     */
    @Override
    public String getID()
    {
        return getClass().getSimpleName();
    }

    @Override
    public List<Class<? extends WindupRuleProvider>> getExecuteAfter()
    {
        if (previousPhase == null)
        {
            return super.getExecuteAfter();
        }
        return asClassList(previousPhase);
    }

    @Override
    public List<Class<? extends WindupRuleProvider>> getExecuteBefore()
    {
        if (nextPhase == null)
        {
            return super.getExecuteBefore();
        }
        return asClassList(nextPhase);
    }

    /**
     * This allows the sorter to link {@link RulePhase}s to each other, without them having to explicitly specify both the executeAfter and
     * executeBefore.
     */
    public void setExecuteAfter(Class<? extends RulePhase> previousPhase)
    {
        this.previousPhase = previousPhase;
    }

    /**
     * This allows the sorter to link {@link RulePhase}s to each other, without them having to explicitly specify both the executeAfter and
     * executeBefore.
     */
    public void setExecuteBefore(Class<? extends RulePhase> nextPhase)
    {
        this.nextPhase = nextPhase;
    }
}
