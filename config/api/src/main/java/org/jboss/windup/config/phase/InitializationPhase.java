package org.jboss.windup.config.phase;

/**
 * Next: {@link DiscoveryPhase}
 *
 * <p>
 * This is the first phase of Windup Execution. Initialization related tasks (such as copying configuration data to the
 * graph) should occur during this phase.
 * </p>
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class InitializationPhase extends RulePhase {
    public InitializationPhase() {
        super(InitializationPhase.class);
    }

    @Override
    public Class<? extends RulePhase> getExecuteAfter() {
        return null;
    }

    @Override
    public Class<? extends RulePhase> getExecuteBefore() {
        return null;
    }
}
