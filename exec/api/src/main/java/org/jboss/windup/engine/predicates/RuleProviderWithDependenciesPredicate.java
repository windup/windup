package org.jboss.windup.engine.predicates;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import org.jboss.forge.furnace.util.Predicate;
import org.jboss.windup.config.WindupRuleProvider;

/**
 * Executes only the given rule with all it's dependencies and pre-phases.
 * 
 * @author mbriskar
 *
 */
public class RuleProviderWithDependenciesPredicate implements Predicate<WindupRuleProvider>
{
    private static Logger LOG = Logger.getLogger(RuleProviderWithDependenciesPredicate.class.getName());

    private List<WindupRuleProvider> ruleProviders;

    @SuppressWarnings("unchecked")
    public RuleProviderWithDependenciesPredicate(Class<? extends WindupRuleProvider> ruleProviderClass)
                throws InstantiationException, IllegalAccessException
    {
        ruleProviders = (List<WindupRuleProvider>) Collections.singletonList(ruleProviderClass.newInstance());
    }

    @SafeVarargs
    public RuleProviderWithDependenciesPredicate(Class<? extends WindupRuleProvider>... ruleProviderClass)
                throws InstantiationException, IllegalAccessException
    {
        ruleProviders = new ArrayList<>(ruleProviderClass.length);
        for (Class<? extends WindupRuleProvider> clz : ruleProviderClass)
        {
            ruleProviders.add(clz.newInstance());
        }

    }

    @Override
    public boolean accept(WindupRuleProvider type)
    {
        int typeExecutionIndex = type.getExecutionIndex();
        for (WindupRuleProvider ruleProvider : this.ruleProviders)
        {
            int otherExecutionIndex = ruleProvider.getExecutionIndex();
            if (otherExecutionIndex <= typeExecutionIndex)
            {
                LOG.info("Accepting provider: " + type.getID());
                // is in the pre-phase
                return true;
            }
            else
            {
                List<Class<? extends WindupRuleProvider>> executeAfter = ruleProvider.getExecuteAfter();
                List<String> executeAfterIDs = ruleProvider.getExecuteAfterIDs();
                if ((executeAfter.contains(type.getClass())) || executeAfterIDs.contains(type.getID()))
                {
                    LOG.info("Accepting provider: " + type.getID());
                    // is a dependency and are in the same phase
                    return true;
                }
                for (Class<? extends WindupRuleProvider> ruleProviderClassAfter : executeAfter)
                {
                    if (ruleProviderClassAfter.isAssignableFrom(type.getClass()))
                    {
                        LOG.info("Accepting provider: " + type.getID());
                        return true;
                    }
                }
                if (ruleProvider.getClass().isAssignableFrom(type.getClass()))
                {
                    LOG.info("Accepting provider: " + type.getID());
                    // is the given rule provider
                    return true;
                }
            }
        }
        LOG.info("Skipping provider: " + type.getID());
        return false;
    }

}