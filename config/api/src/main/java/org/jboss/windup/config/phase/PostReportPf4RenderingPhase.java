package org.jboss.windup.config.phase;

public class PostReportPf4RenderingPhase extends RulePhase {
    public PostReportPf4RenderingPhase() {
        super(PostReportPf4RenderingPhase.class);
    }

    @Override
    public Class<? extends RulePhase> getExecuteAfter() {
        return ReportPf4RenderingPhase.class;
    }

    @Override
    public Class<? extends RulePhase> getExecuteBefore() {
        return null;
    }
}
