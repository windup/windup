package org.jboss.windup.config.phase;

/**
 * Previous: {@link ClassifyFileTypesPhase}<br/>
 * Next: {@link DecompilationPhase}
 *
 * <p>
 * Discovering of the project structure of the input application will occur during this phase. This will discover which
 * files are in which project (including determining subprojects) and analyze as much metadata as possible for supported
 * project types (for example, Maven projects).
 * </p>
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class DiscoverProjectStructurePhase extends RulePhase {
    public DiscoverProjectStructurePhase() {
        super(DiscoverProjectStructurePhase.class);
    }

    @Override
    public Class<? extends RulePhase> getExecuteAfter() {
        return ClassifyFileTypesPhase.class;
    }

    @Override
    public Class<? extends RulePhase> getExecuteBefore() {
        return null;
    }
}
