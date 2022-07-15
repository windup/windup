package org.jboss.windup.config.phase;


/**
 * Previous: {@link ReportGenerationPhase}<br/>
 * Next: {@link ReportRenderingPhase}
 *
 * <p>
 * This occurs immediately after the main tasks of report generation. This can be used to generate reports that will need data from all of the other
 * reports that have been previously generated.
 * </p>
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class PostReportGenerationPhase extends RulePhase {
    public PostReportGenerationPhase() {
        super(PostReportGenerationPhase.class);
    }

    @Override
    public Class<? extends RulePhase> getExecuteAfter() {
        return ReportGenerationPhase.class;
    }

    @Override
    public Class<? extends RulePhase> getExecuteBefore() {
        return null;
    }
}
