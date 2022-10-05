package org.jboss.windup.config.phase;


/**
 * Previous: {@link DiscoverProjectStructurePhase}<br/>
 * Next: {@link InitialAnalysisPhase}
 *
 * <p>
 * Any required decompilation of an input application would occur during this phase.
 * </p>
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class DecompilationPhase extends RulePhase {

    public DecompilationPhase() {
        super(DecompilationPhase.class);
    }

    @Override
    public Class<? extends RulePhase> getExecuteAfter() {
        return DiscoverProjectStructurePhase.class;
    }

    @Override
    public Class<? extends RulePhase> getExecuteBefore() {
        return null;
    }
}
