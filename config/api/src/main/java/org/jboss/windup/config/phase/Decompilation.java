package org.jboss.windup.config.phase;

import java.util.List;

import org.jboss.windup.config.WindupRuleProvider;

/**
 * Previous: {@link ClassifyFileTypes}<br/>
 * Next: {@link DiscoverProjectStructure}
 * 
 * <p>
 * Any required decompilation of an input application would occur during this phase.
 * </p>
 * 
 * @author jsightler
 *
 */
public class Decompilation extends RulePhase
{

    @Override
    public List<Class<? extends WindupRuleProvider>> getExecuteAfter()
    {
        return asClassList(ClassifyFileTypes.class);
    }
}
