package org.jboss.windup.config.phase;

public class ReportPf4RenderingPhase extends RulePhase {
    public ReportPf4RenderingPhase() {
        super(ReportPf4RenderingPhase.class);
    }

    @Override
    public Class<? extends RulePhase> getExecuteAfter() {
        return PostReportGenerationPhase.class;
    }

    @Override
    public Class<? extends RulePhase> getExecuteBefore() {
        return null;
    }
}
