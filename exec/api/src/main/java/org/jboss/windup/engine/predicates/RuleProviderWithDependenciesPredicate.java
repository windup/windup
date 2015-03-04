package org.jboss.windup.engine.predicates;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import org.jboss.forge.furnace.util.Predicate;
import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.RuleProvider;

/**
 * Executes only the given rule with all it's dependencies and pre-phases.
 *
 * @author mbriskar
 *
 */
public class RuleProviderWithDependenciesPredicate implements Predicate<AbstractRuleProvider>
{
    private static Logger LOG = Logger.getLogger(RuleProviderWithDependenciesPredicate.class.getName());

    private List<AbstractRuleProvider> ruleProviders;

    @SuppressWarnings("unchecked")
    public RuleProviderWithDependenciesPredicate(Class<? extends AbstractRuleProvider> ruleProviderClass)
                throws InstantiationException, IllegalAccessException
    {
        ruleProviders = (List<AbstractRuleProvider>) Collections.singletonList(ruleProviderClass.newInstance());
    }

    @SafeVarargs
    public RuleProviderWithDependenciesPredicate(Class<? extends AbstractRuleProvider>... ruleProviderClass)
                throws InstantiationException, IllegalAccessException
    {
        ruleProviders = new ArrayList<>(ruleProviderClass.length);
        for (Class<? extends AbstractRuleProvider> clz : ruleProviderClass)
        {
            ruleProviders.add(clz.newInstance());
        }

    }

    @Override
    public boolean accept(AbstractRuleProvider type)
    {
        int typeExecutionIndex = type.getExecutionIndex();
        for (AbstractRuleProvider ruleProvider : this.ruleProviders)
        {
            int otherExecutionIndex = ruleProvider.getExecutionIndex();
            if (otherExecutionIndex <= typeExecutionIndex)
            {
                // is in the pre-phase
                LOG.fine("Accepting provider: " + type.getMetadata().getID());
                return true;
            }
            else
            {
                List<Class<? extends RuleProvider>> executeAfter = ruleProvider.getMetadata().getExecuteAfter();
                List<String> executeAfterIDs = ruleProvider.getMetadata().getExecuteAfterIDs();
                if ((executeAfter.contains(type.getClass())) || executeAfterIDs.contains(type.getMetadata().getID()))
                {
                    // is a dependency and are in the same phase
                    LOG.fine("Accepting provider: " + type.getMetadata().getID());
                    return true;
                }
                for (Class<? extends RuleProvider> ruleProviderClassAfter : executeAfter)
                {
                    if (ruleProviderClassAfter.isAssignableFrom(type.getClass()))
                    {
                        LOG.fine("Accepting provider: " + type.getMetadata().getID());
                        return true;
                    }
                }
                if (ruleProvider.getClass().isAssignableFrom(type.getClass()))
                {
                    // is the given rule provider
                    LOG.fine("Accepting provider: " + type.getMetadata().getID());
                    return true;
                }
            }
        }

        LOG.fine("Skipping provider: " + type.getMetadata().getID());
        return false;
    }

}