package org.jboss.windup.config.phase;

import org.ocpsoft.rewrite.config.Rule;

/**
 * Previous: {@link DecompilationPhase}<br/>
 * Next: {@link MigrationRulesPhase}
 *
 * <p>
 * This phase occurs after the application has been unzipped, files have been discovered (including basic filetype
 * information), and the project structure has been ascertained. {@link Rule}s from this phase will perform tasks such
 * as the analysis of source code for placement within the graph (for use by later {@link Rule}s).
 * </p>
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class InitialAnalysisPhase extends RulePhase {
    public InitialAnalysisPhase() {
        super(InitialAnalysisPhase.class);
    }

    @Override
    public Class<? extends RulePhase> getExecuteAfter() {
        return DecompilationPhase.class;
    }

    @Override
    public Class<? extends RulePhase> getExecuteBefore() {
        return null;
    }
}
