package org.jboss.windup.config.phase;

import java.util.List;

import org.jboss.windup.config.WindupRuleProvider;

/**
 * Previous: {@link ReportGeneration}<br/>
 * Next: {@link ReportRendering}
 * 
 * <p>
 * This occurs immediately after the main tasks of report generation. This can be used to generate reports that will need data from all of the other
 * reports that have been previously generated.
 * </p>
 * 
 * @author jsightler
 *
 */
public class PostReportGeneration extends RulePhase
{
    @Override
    public List<Class<? extends WindupRuleProvider>> getExecuteAfter()
    {
        return asClassList(ReportGeneration.class);
    }
}
