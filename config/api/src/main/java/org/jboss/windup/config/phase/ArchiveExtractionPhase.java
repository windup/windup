package org.jboss.windup.config.phase;

/**
 * Previous: {@link DiscoveryPhase}<br/>
 * Next: {@link ArchiveMetadataExtractionPhase}
 *
 * <p>
 * Unzipping of any input files (such as an incoming ear file) occur during this phase.
 * </p>
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class ArchiveExtractionPhase extends RulePhase {
    public ArchiveExtractionPhase() {
        super(ArchiveExtractionPhase.class);
    }

    @Override
    public Class<? extends RulePhase> getExecuteAfter() {
        return DiscoveryPhase.class;
    }

    @Override
    public Class<? extends RulePhase> getExecuteBefore() {
        return null;
    }
}
