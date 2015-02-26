package org.jboss.windup.config.phase;

import java.util.List;

import org.jboss.windup.config.WindupRuleProvider;

/**
 * Previous: {@link PreReportGenerationPhase}<br/>
 * Next: {@link PostReportGenerationPhase}
 * 
 * <p>
 * During this phase, report information will be gathered and stored in the graph.
 * </p>
 * 
 * @author jsightler
 *
 */
public class ReportGenerationPhase extends RulePhase
{

    @Override
    public List<Class<? extends WindupRuleProvider>> getExecuteAfter()
    {
        return asClassList(PreReportGenerationPhase.class);
    }
}
