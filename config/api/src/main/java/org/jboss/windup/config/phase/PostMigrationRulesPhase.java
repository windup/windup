package org.jboss.windup.config.phase;

/**
 * Previous: {@link MigrationRulesPhase}<br/>
 * Next: {@link PreReportGenerationPhase}
 *
 * <p>
 * This occurs immediately after {@link MigrationRulesPhase}. This can be used in cases where some rule wants to execute
 * immediately after all other migration rules. The primary use case at the moment involves unit tests.
 * </p>
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class PostMigrationRulesPhase extends RulePhase {
    public PostMigrationRulesPhase() {
        super(PostMigrationRulesPhase.class);
    }

    @Override
    public Class<? extends RulePhase> getExecuteAfter() {
        return MigrationRulesPhase.class;
    }

    @Override
    public Class<? extends RulePhase> getExecuteBefore() {
        return null;
    }
}
