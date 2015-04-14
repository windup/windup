package org.jboss.windup.config.phase;

import org.jboss.windup.config.AbstractRuleProvider;
import org.ocpsoft.rewrite.config.Rule;

/**
 * Previous: {@link PostReportRenderingPhase}<br/>
 * Next: {@link PostFinalizePhase}
 * 
 * <p>
 * This occurs at the end of execution. {@link Rule}s in this phase are responsible for any cleanup of resources that
 * may have been opened during {@link Rule}s from earlier {@link AbstractRuleProvider}s.
 * </p>
 * 
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 *
 */
public class FinalizePhase extends RulePhase
{
    public FinalizePhase()
    {
        super(FinalizePhase.class);
    }

    @Override
    public Class<? extends RulePhase> getExecuteAfter()
    {
        return PostReportRenderingPhase.class;
    }

    @Override
    public Class<? extends RulePhase> getExecuteBefore()
    {
        return null;
    }
}
