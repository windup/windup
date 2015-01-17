package org.jboss.windup.config.phase;

import java.util.List;

import org.jboss.windup.config.WindupRuleProvider;

/**
 * This occurs at the end of execution. {@link Rule}s in this phase are responsible for any cleanup of resources that may have been opened during
 * {@link Rule}s from earlier {@link WindupRuleProvider}s.
 * 
 * @author jsightler
 *
 */
public class Finalize extends RulePhase
{
    @Override
    public List<Class<? extends WindupRuleProvider>> getExecuteAfter()
    {
        return asClassList(PostReportRendering.class);
    }
}
