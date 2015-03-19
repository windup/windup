package org.jboss.windup.engine.predicates;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.RuleProvider;
import org.jboss.windup.exec.rulefilters.RuleProviderFilter;

/**
 * Executes only the given rule with all it's dependencies and pre-phases.
 *
 * @author mbriskar
 *
 */
public class RuleProviderWithDependenciesPredicate implements RuleProviderFilter
{
    private static Logger LOG = Logger.getLogger(RuleProviderWithDependenciesPredicate.class.getName());

    private List<RuleProvider> ruleProviders;

    @SafeVarargs
    public RuleProviderWithDependenciesPredicate(Class<? extends RuleProvider> provider, Class<? extends RuleProvider>... providers)
                throws InstantiationException, IllegalAccessException
    {
        /*
         * FIXME This assumes that the providers can be instantiated with the default constructor. It should really
         * request an instance from Furance.
         */
        ruleProviders = new ArrayList<>();
        ruleProviders.add(provider.newInstance());
        for (Class<? extends RuleProvider> clazz : providers)
        {
            ruleProviders.add(clazz.newInstance());
        }
    }

    @Override
    public boolean accept(RuleProvider provider)
    {
        if (provider instanceof AbstractRuleProvider)
        {
            // FIXME This execution index API needs to be exposed publicly or handled via another pattern.
            int typeExecutionIndex = ((AbstractRuleProvider) provider).getExecutionIndex();
            for (RuleProvider ruleProvider : this.ruleProviders)
            {
                int otherExecutionIndex = ((AbstractRuleProvider) ruleProvider).getExecutionIndex();
                if (otherExecutionIndex <= typeExecutionIndex)
                {
                    LOG.fine("Accepting provider: " + provider.getMetadata().getID());
                    return true;
                }
                else
                {
                    List<Class<? extends RuleProvider>> executeAfter = ruleProvider.getMetadata().getExecuteAfter();
                    List<String> executeAfterIDs = ruleProvider.getMetadata().getExecuteAfterIDs();
                    if ((executeAfter.contains(provider.getClass())) || executeAfterIDs.contains(provider.getMetadata().getID()))
                    {
                        LOG.fine("Accepting provider: " + provider.getMetadata().getID());
                        return true;
                    }
                    for (Class<? extends RuleProvider> afterType : executeAfter)
                    {
                        if (afterType.isAssignableFrom(provider.getClass()))
                        {
                            LOG.fine("Accepting provider: " + provider.getMetadata().getID());
                            return true;
                        }
                    }
                    if (ruleProvider.getClass().isAssignableFrom(provider.getClass()))
                    {
                        LOG.fine("Accepting provider: " + provider.getMetadata().getID());
                        return true;
                    }
                }
            }

            LOG.fine("Skipping provider: " + provider.getMetadata().getID());
        }
        return false;
    }

}