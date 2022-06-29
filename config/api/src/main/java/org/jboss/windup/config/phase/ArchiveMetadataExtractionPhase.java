package org.jboss.windup.config.phase;


/**
 * Previous: {@link ArchiveExtractionPhase}<br />
 * Next: {@link ClassifyFileTypesPhase}
 *
 * <p>
 * Occurs immediately after archive extraction. This handles things like calculating checksums for archives or
 * determining the specific type of archive (ear vs other types of archives).
 * </p>
 */
public class ArchiveMetadataExtractionPhase extends RulePhase {
    public ArchiveMetadataExtractionPhase() {
        super(ArchiveMetadataExtractionPhase.class);
    }

    @Override
    public Class<? extends RulePhase> getExecuteAfter() {
        return ArchiveExtractionPhase.class;
    }

    @Override
    public Class<? extends RulePhase> getExecuteBefore() {
        return null;
    }
}
