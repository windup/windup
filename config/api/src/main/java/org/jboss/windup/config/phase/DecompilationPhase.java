package org.jboss.windup.config.phase;

import java.util.List;

import org.jboss.windup.config.WindupRuleProvider;

/**
 * Previous: {@link DiscoverProjectStructurePhase}<br/>
 * Next: {@link InitialAnalysisPhase}
 * 
 * <p>
 * Any required decompilation of an input application would occur during this phase.
 * </p>
 * 
 * @author jsightler
 *
 */
public class DecompilationPhase extends RulePhase
{

    @Override
    public List<Class<? extends WindupRuleProvider>> getExecuteAfter()
    {
        return asClassList(DiscoverProjectStructurePhase.class);
    }
}
