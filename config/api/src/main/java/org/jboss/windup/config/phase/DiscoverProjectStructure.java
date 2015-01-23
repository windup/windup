package org.jboss.windup.config.phase;

import java.util.List;

import org.jboss.windup.config.WindupRuleProvider;

/**
 * Discovering of the project structure of the input application will occur during this phase.
 * 
 * @author jsightler
 *
 */
public class DiscoverProjectStructure extends RulePhase
{

    @Override
    public List<Class<? extends WindupRuleProvider>> getExecuteAfter()
    {
        return asClassList(ClassifyFileTypes.class);
    }
}
