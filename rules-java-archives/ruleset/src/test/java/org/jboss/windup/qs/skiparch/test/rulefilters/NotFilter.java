package org.jboss.windup.qs.skiparch.test.rulefilters;


import org.jboss.forge.furnace.util.Predicate;
import org.jboss.windup.config.WindupRuleProvider;

/**
 * NOT predicate which negates the result of given predicate..
 *
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public class NotFilter implements RuleFilter
{
    protected final Predicate predicate;


    public NotFilter(Predicate pred)
    {
        this.predicate = pred;
    }

    @Override
    public boolean accept(WindupRuleProvider provider)
    {
        return ! this.predicate.accept(provider);
    }

}
