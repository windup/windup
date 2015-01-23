package org.jboss.windup.config.phase;

import java.util.List;

import org.jboss.windup.config.WindupRuleProvider;

/**
 * Reports will be rendered to the disk during this phase.
 * 
 * @author jsightler
 *
 */
public class ReportRendering extends RulePhase
{
    @Override
    public List<Class<? extends WindupRuleProvider>> getExecuteAfter()
    {
        return asClassList(PostReportGeneration.class);
    }
}
