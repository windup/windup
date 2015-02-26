package org.jboss.windup.config.phase;

import java.util.List;

import org.jboss.windup.config.WindupRuleProvider;

/**
 * Previous: {@link PostReportRenderingPhase}<br/>
 * Next: {@link PostFinalizePhase}
 * 
 * <p>
 * This occurs at the end of execution. {@link Rule}s in this phase are responsible for any cleanup of resources that may have been opened during
 * {@link Rule}s from earlier {@link WindupRuleProvider}s.
 * </p>
 * 
 * @author jsightler
 *
 */
public class FinalizePhase extends RulePhase
{
    @Override
    public List<Class<? extends WindupRuleProvider>> getExecuteAfter()
    {
        return asClassList(PostReportRenderingPhase.class);
    }
}
