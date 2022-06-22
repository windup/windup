package org.jboss.windup.config.phase;

import org.ocpsoft.rewrite.config.Rule;

/**
 * Previous: {@link InitialAnalysisPhase}<br/>
 * Next: {@link PostMigrationRulesPhase}
 *
 * <p>
 * Most {@link Rule}s will go in this {@link RulePhase}. These include {@link Rule}s that detect code in the source
 * application (or server) that will need to be changed and produce metadata to be reported on regarding these changes.
 * </p>
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class MigrationRulesPhase extends RulePhase {
    public MigrationRulesPhase() {
        super(MigrationRulesPhase.class);
    }

    @Override
    public Class<? extends RulePhase> getExecuteAfter() {
        return InitialAnalysisPhase.class;
    }

    @Override
    public Class<? extends RulePhase> getExecuteBefore() {
        return null;
    }
}
