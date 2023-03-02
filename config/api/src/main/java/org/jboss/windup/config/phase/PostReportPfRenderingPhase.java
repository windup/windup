package org.jboss.windup.config.phase;

public class PostReportPfRenderingPhase extends RulePhase {
    public PostReportPfRenderingPhase() {
        super(PostReportPfRenderingPhase.class);
    }

    @Override
    public Class<? extends RulePhase> getExecuteAfter() {
        return ReportPfRenderingPhase.class;
    }

    @Override
    public Class<? extends RulePhase> getExecuteBefore() {
        return null;
    }
}
