package org.jboss.windup.engine.predicates;

import java.util.List;

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

    private WindupRuleProvider ruleProvider;

    public RuleProviderWithDependenciesPredicate(Class<? extends WindupRuleProvider> ruleProviderClass)
                throws InstantiationException, IllegalAccessException
    {
        ruleProvider = ruleProviderClass.newInstance();
    }

    @Override
    public boolean accept(WindupRuleProvider type)
    {
        int compareTo = type.getPhase().compareTo(ruleProvider.getPhase());
        if (compareTo < 0)
        {
            // is in the pre-phase
            return true;
        }
        else if (compareTo == 0)
        {
            List<Class<? extends WindupRuleProvider>> executeAfter = ruleProvider.getExecuteAfter();
            List<String> executeAfterIDs = ruleProvider.getExecuteAfterIDs();
            if ((executeAfter.contains(type.getClass())) || executeAfterIDs.contains(type.getID()))
            {
                // is a dependency and are in the same phase
                return true;
            }
            for (Class<? extends WindupRuleProvider> ruleProviderClassAfter : executeAfter)
            {
                if (ruleProviderClassAfter.isAssignableFrom(type.getClass()))
                {
                    return true;
                }
            }
            if (ruleProvider.getClass().isAssignableFrom(type.getClass()))
            {
                // is the given rule provider
                return true;
            }
        }
        return false;
    }

}
