package org.jboss.windup.config.phase;

import java.util.List;

import org.jboss.windup.config.WindupRuleProvider;

/**
 * Previous: {@link ReportRenderingPhase}<br/>
 * Next: {@link FinalizePhase}
 * 
 * <p>
 * This occurs immediately after reports have been rendered. It can be used to render any reports that need to execute last. One possible use is to
 * render all of the contents of the graph itself.
 * </p>
 * 
 * @author jsightler
 *
 */
public class PostReportRenderingPhase extends RulePhase
{

    @Override
    public List<Class<? extends WindupRuleProvider>> getExecuteAfter()
    {
        return asClassList(ReportRenderingPhase.class);
    }
}
