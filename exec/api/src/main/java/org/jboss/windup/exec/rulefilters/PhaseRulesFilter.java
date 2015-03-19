package org.jboss.windup.exec.rulefilters;


import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.jboss.windup.config.RuleProvider;
import org.jboss.windup.config.phase.ReportGenerationPhase;
import org.jboss.windup.config.phase.ReportRenderingPhase;
import org.jboss.windup.config.phase.RulePhase;

/**
 * Filters the rules with given phases.
 * This does NOT cover rules with DependentPhase which end up being executed in given phases.
 *
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public class PhaseRulesFilter implements RuleProviderFilter
{
    private final Set<Class<? extends RulePhase>> phases;

    public PhaseRulesFilter( Class<? extends RulePhase> ... phases )
    {
        this.phases = new HashSet(Arrays.asList(phases));
    }


    @Override
    public boolean accept(RuleProvider provider)
    {
        return this.phases.contains( provider.getMetadata().getPhase() );
    }


    /**
     * Filters the rules with phase = Reporting*.
     *
     * @author Ondrej Zizka, ozizka at redhat.com
     */
    public static class ReportingRulesFilter extends PhaseRulesFilter
    {
        public ReportingRulesFilter()
        {
            super(ReportGenerationPhase.class, ReportRenderingPhase.class);
        }
    }

}// class
