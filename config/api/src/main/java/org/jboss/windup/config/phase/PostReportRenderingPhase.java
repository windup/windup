package org.jboss.windup.config.phase;


/**
 * Previous: {@link ReportRenderingPhase}<br/>
 * Next: {@link FinalizePhase}
 *
 * <p>
 * This occurs immediately after reports have been rendered. It can be used to render any reports that need to execute
 * last. One possible use is to render all of the contents of the graph itself.
 * </p>
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class PostReportRenderingPhase extends RulePhase {
    public PostReportRenderingPhase() {
        super(PostReportRenderingPhase.class);
    }

    @Override
    public Class<? extends RulePhase> getExecuteAfter() {
        return ReportRenderingPhase.class;
    }

    @Override
    public Class<? extends RulePhase> getExecuteBefore() {
        return null;
    }
}
