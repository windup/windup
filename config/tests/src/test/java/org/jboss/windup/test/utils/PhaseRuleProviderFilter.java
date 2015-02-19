package org.jboss.windup.test.utils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.jboss.forge.furnace.util.Predicate;
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.config.phase.RulePhase;

/**
 * Accepts only those RuleProviders which have one of given phases.
 *
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public class PhaseRuleProviderFilter implements Predicate<WindupRuleProvider>
{
    private final Set<Class<? extends RulePhase>> allowedPhases;


    public PhaseRuleProviderFilter(Class<? extends RulePhase>... phases)
    {
        this.allowedPhases = new HashSet(Arrays.asList(phases));
    }


    @Override
    public boolean accept(WindupRuleProvider provider)
    {
        return this.allowedPhases.contains(provider.getPhase());
    }

}
