package org.jboss.windup.config.phase;

import java.util.List;

import org.jboss.windup.config.WindupRuleProvider;

/**
 * During this phase, report information will be gathered and stored in the graph.
 * 
 * @author jsightler
 *
 */
public class ReportGeneration extends RulePhase
{

    @Override
    public List<Class<? extends WindupRuleProvider>> getExecuteAfter()
    {
        return asClassList(PreReportGeneration.class);
    }
}
