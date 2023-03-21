package org.jboss.windup.config.phase;

public class PreReportPfRenderingPhase extends RulePhase {
    public PreReportPfRenderingPhase() {
        super(PreReportPfRenderingPhase.class);
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
