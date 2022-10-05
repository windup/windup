package org.jboss.windup.graph.model;

import org.apache.tinkerpop.gremlin.structure.Direction;
import org.jboss.windup.graph.Adjacency;

/**
 * Indicates that this {@link ArchiveModel} is actually a duplicate of another archive. This will be linked
 * back to the canonical archive.
 * <p>
 * The duplicate will generally contain no files and the canonical archive ({@see DuplicateArchiveModel#getCanonicalArchive})
 * should be used for finding the included files.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
@TypeValue(DuplicateArchiveModel.TYPE)
public interface DuplicateArchiveModel extends ArchiveModel {
    String TYPE = "DuplicateArchiveModel";

    String CANONICAL_ARCHIVE = TYPE + "-canonicalArchive";

    /**
     * Contains a link to the canonical archive from this duplicate instance.
     */
    @Adjacency(label = CANONICAL_ARCHIVE, direction = Direction.OUT)
    ArchiveModel getCanonicalArchive();

    /**
     * Contains a link to the canonical archive from this duplicate instance.
     */
    @Adjacency(label = CANONICAL_ARCHIVE, direction = Direction.OUT)
    DuplicateArchiveModel setCanonicalArchive(ArchiveModel original);
}
