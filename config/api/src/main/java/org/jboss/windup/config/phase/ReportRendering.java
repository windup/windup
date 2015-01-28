package org.jboss.windup.config.phase;

import java.util.List;

import org.jboss.windup.config.WindupRuleProvider;

/**
 * Previous: {@link PostReportGeneration}<br/>
 * Next: {@link PostReportRendering}
 * 
 * <p>
 * Reports will be rendered to the disk during this phase.
 * </p>
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
