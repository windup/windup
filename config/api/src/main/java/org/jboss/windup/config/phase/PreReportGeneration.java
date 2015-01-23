package org.jboss.windup.config.phase;

import java.util.List;

import org.jboss.windup.config.WindupRuleProvider;

/**
 * This occurs immediately before {@link ReportGeneration} and can be used for initialization related tasks that will be needed by all reports during
 * {@link ReportGeneration}.
 * 
 * @author jsightler
 *
 */
public class PreReportGeneration extends RulePhase
{

    @Override
    public List<Class<? extends WindupRuleProvider>> getExecuteAfter()
    {
        return asClassList(MigrationRules.class);
    }
}
