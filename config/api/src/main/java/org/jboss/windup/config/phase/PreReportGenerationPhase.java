package org.jboss.windup.config.phase;


/**
 * Previous: {@link PostMigrationRulesPhase}<br/>
 * Next: {@link ReportGenerationPhase}
 *
 * <p>
 * This occurs immediately before {@link ReportGenerationPhase} and can be used for initialization related tasks that will be needed by all reports during
 * {@link ReportGenerationPhase}.
 * </p>
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class PreReportGenerationPhase extends RulePhase {
    public PreReportGenerationPhase() {
        super(PreReportGenerationPhase.class);
    }

    @Override
    public Class<? extends RulePhase> getExecuteAfter() {
        return PostMigrationRulesPhase.class;
    }

    @Override
    public Class<? extends RulePhase> getExecuteBefore() {
        return null;
    }
}
