package org.jboss.windup.exec.rulefilters;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.jboss.forge.furnace.util.Predicate;
import org.jboss.windup.config.RuleProvider;
import org.jboss.windup.config.phase.ReportGenerationPhase;
import org.jboss.windup.config.phase.ReportRenderingPhase;
import org.jboss.windup.config.phase.RulePhase;

/**
 * Filters the rules with given phases.
 *
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public class RuleProviderPhasePredicate implements Predicate<RuleProvider>
{
    private final Set<Class<? extends RulePhase>> phases;

    /**
     * Creates the {@link RuleProviderPhasePredicate} with the given phase types.
     */
    public RuleProviderPhasePredicate(Class<? extends RulePhase>... phases)
    {
        this.phases = new HashSet(Arrays.asList(phases));
    }

    @Override
    public boolean accept(RuleProvider provider)
    {
        return this.phases.contains(provider.getMetadata().getPhase());
    }

    /**
     * Filters the rules with phase = Reporting*.
     *
     * @author Ondrej Zizka, ozizka at redhat.com
     */
    public static class ReportingFilterRuleProvider extends RuleProviderPhasePredicate
    {
        public ReportingFilterRuleProvider()
        {
            super(ReportGenerationPhase.class, ReportRenderingPhase.class);
        }
    }

}
