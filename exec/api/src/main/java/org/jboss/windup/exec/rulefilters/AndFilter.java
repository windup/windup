package org.jboss.windup.exec.rulefilters;


import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.jboss.forge.furnace.util.Predicate;
import org.jboss.windup.config.RuleProvider;

/**
 * AND predicate which needs all of the given predicates to accept.
 * It will stop on first false if you setStopWhenKnown(true).
 *
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public class AndFilter implements RuleProviderFilter
{
    protected final Set<Predicate> predicates;
    protected boolean stopWhenKnown = false;


    public AndFilter(Predicate ... preds)
    {
        this.predicates = new HashSet(Arrays.asList(preds));
    }

    @Override
    public boolean accept(RuleProvider provider)
    {
        boolean res = true;
        if (this.predicates.isEmpty())
            return false;

        for( Predicate pred : this.predicates )
        {
            if (!pred.accept(provider)){
                res = false;
                if(this.stopWhenKnown)
                    return false;
            }
        }
        return res;
    }


    public void setStopWhenKnown(boolean stopWhenKnown)
    {
        this.stopWhenKnown = stopWhenKnown;
    }

}// class
