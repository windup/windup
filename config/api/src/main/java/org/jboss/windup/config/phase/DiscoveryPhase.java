package org.jboss.windup.config.phase;

/**
 * Previous: {@link InitializationPhase}<br/>
 * Next: {@link ArchiveExtractionPhase}
 *
 * <p>
 * This discovers files from the input (for example, find all of the files in the input directory).
 * </p>
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class DiscoveryPhase extends RulePhase {
    public DiscoveryPhase() {
        super(DiscoveryPhase.class);
    }

    @Override
    public Class<? extends RulePhase> getExecuteAfter() {
        return InitializationPhase.class;
    }

    @Override
    public Class<? extends RulePhase> getExecuteBefore() {
        return null;
    }
}
