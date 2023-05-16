package org.jboss.windup.config.phase;

public class ReportPfRenderingPhase extends RulePhase {
    public ReportPfRenderingPhase() {
        super(ReportPfRenderingPhase.class);
    }

    @Override
    public Class<? extends RulePhase> getExecuteAfter() {
        return PreReportPfRenderingPhase.class;
    }

    @Override
    public Class<? extends RulePhase> getExecuteBefore() {
        return null;
    }
}
