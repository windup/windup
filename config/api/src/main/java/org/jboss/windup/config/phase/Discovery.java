package org.jboss.windup.config.phase;

import java.util.List;

import org.jboss.windup.config.WindupRuleProvider;

/**
 * Previous: {@link Initialization}<br/>
 * Next: {@link ArchiveExtraction}
 * 
 * <p>
 * This discovers files from the input (for example, find all of the files in the input directory).
 * </p>
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
