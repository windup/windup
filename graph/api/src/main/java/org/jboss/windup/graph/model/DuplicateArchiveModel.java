package org.jboss.windup.graph.model;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

/**
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
@TypeValue(DuplicateArchiveModel.TYPE)
public interface DuplicateArchiveModel extends ArchiveModel
{
    String TYPE = "DuplicateArchive";

    String ORIGINAL_ARCHIVE = "originalArchive";

    @Adjacency(label = ORIGINAL_ARCHIVE, direction = Direction.OUT)
    ArchiveModel getOriginalArchive();

    @Adjacency(label = ORIGINAL_ARCHIVE, direction = Direction.OUT)
    DuplicateArchiveModel setOriginalArchive(ArchiveModel original);
}
