package org.jboss.windup.config.phase;


/**
 * Previous: {@link ArchiveMetadataExtractionPhase}<br/>
 * Next: {@link DiscoverProjectStructurePhase}<br/>
 *
 * <p>
 * This scans files and attaches metadata to them. For example, this may find all of the Java files in an application
 * and mark them as Java, or it may find all of the bash scripts in an input and identify them appropriately.
 * </p>
 * .
 */
public class ClassifyFileTypesPhase extends RulePhase {
    public ClassifyFileTypesPhase() {
        super(ClassifyFileTypesPhase.class);
    }

    @Override
    public Class<? extends RulePhase> getExecuteAfter() {
        return ArchiveMetadataExtractionPhase.class;
    }

    @Override
    public Class<? extends RulePhase> getExecuteBefore() {
        return null;
    }

}
