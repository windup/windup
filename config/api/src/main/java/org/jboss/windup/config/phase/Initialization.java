package org.jboss.windup.config.phase;

import java.util.List;

import org.jboss.windup.config.WindupRuleProvider;

/**
 * This is the first phase of Windup Execution. Initialization related tasks (such as copying configuration data to the graph) should occur during
 * this phase.
 * 
 * @author jsightler
 *
 */
public class Initialization extends RulePhase
{

    @Override
    public List<Class<? extends WindupRuleProvider>> getExecuteBefore()
    {
        return asClassList(ArchiveExtraction.class);
    }
}
