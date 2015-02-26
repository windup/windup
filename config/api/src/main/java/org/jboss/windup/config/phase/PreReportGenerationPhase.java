package org.jboss.windup.config.phase;

import java.util.List;

import org.jboss.windup.config.WindupRuleProvider;

/**
 * Previous: {@link PostMigrationRulesPhase}<br/>
 * Next: {@link ReportGenerationPhase}
 * 
 * <p>
 * This occurs immediately before {@link ReportGenerationPhase} and can be used for initialization related tasks that will be needed by all reports during
 * {@link ReportGenerationPhase}.
 * </p>
 * 
 * @author jsightler
 *
 */
public class PreReportGenerationPhase extends RulePhase
{

    @Override
    public List<Class<? extends WindupRuleProvider>> getExecuteAfter()
    {
        return asClassList(PostMigrationRulesPhase.class);
    }
}
