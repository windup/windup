package org.jboss.windup.config.phase;

import java.util.List;

import org.jboss.windup.config.WindupRuleProvider;

/**
 * This occurs immediately after reports have been rendered. It can be used to render any reports that need to execute last. One possible use is to
 * render all of the contents of the graph itself.
 * 
 * @author jsightler
 *
 */
public class PostReportRendering extends RulePhase
{

    @Override
    public List<Class<? extends WindupRuleProvider>> getExecuteAfter()
    {
        return asClassList(ReportRendering.class);
    }
}
