package org.jboss.windup.config.phase;

import java.util.List;

import org.jboss.windup.config.WindupRuleProvider;

/**
 * This discovers files from the input (for example, find all of the files in the input directory).
 * 
 * @author jsightler
 *
 */
public class Discovery extends RulePhase
{

    @Override
    public List<Class<? extends WindupRuleProvider>> getExecuteAfter()
    {
        return asClassList(Initialization.class);
    }
}
