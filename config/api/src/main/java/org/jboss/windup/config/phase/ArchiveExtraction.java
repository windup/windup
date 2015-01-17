package org.jboss.windup.config.phase;

import java.util.List;

import org.jboss.windup.config.WindupRuleProvider;

/**
 * Unzipping of any input files (such as an incoming ear file) occur during this phase.
 * 
 * @author jsightler
 *
 */
public class ArchiveExtraction extends RulePhase
{
    @Override
    public List<Class<? extends WindupRuleProvider>> getExecuteAfter()
    {
        return asClassList(Discovery.class);
    }
}
