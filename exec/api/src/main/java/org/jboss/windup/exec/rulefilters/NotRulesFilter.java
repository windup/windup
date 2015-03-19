package org.jboss.windup.exec.rulefilters;


import org.jboss.forge.furnace.util.Predicate;
import org.jboss.windup.config.RuleProvider;
import org.jboss.windup.exec.rulefilters.RuleProviderFilter;

/**
 * NOT predicate which negates the result of given predicate..
 *
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public class NotRulesFilter implements RuleProviderFilter
{
    protected final Predicate predicate;


    public NotRulesFilter(Predicate pred)
    {
        this.predicate = pred;
    }

    @Override
    public boolean accept(RuleProvider provider)
    {
        return ! this.predicate.accept(provider);
    }

}
