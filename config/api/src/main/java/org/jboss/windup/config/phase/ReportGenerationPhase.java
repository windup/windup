package org.jboss.windup.config.phase;

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
    public ReportGenerationPhase()
    {
        super(ReportGenerationPhase.class);
    }

    @Override
    public Class<? extends RulePhase> getExecuteAfter()
    {
        return PreReportGenerationPhase.class;
    }

    @Override
    public Class<? extends RulePhase> getExecuteBefore()
    {
        return null;
    }
}
