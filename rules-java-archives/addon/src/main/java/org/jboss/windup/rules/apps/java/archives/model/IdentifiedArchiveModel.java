package org.jboss.windup.rules.apps.java.archives.model;

import org.jboss.windup.graph.model.ArchiveModel;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

/**
 * An {@link IdentifiedArchiveModel} has a {@link ArchiveCoordinateModel}.
 *
 * @author <a href="mailto:ozizka@redhat.com">Ondrej Zizka</a>
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
@TypeValue(IdentifiedArchiveModel.TYPE)
public interface IdentifiedArchiveModel extends ArchiveModel
{
    String TYPE = "identifiedArchive:";
    String COORDINATE = TYPE + "coordinate";

    /**
     * Contains the Maven GAV, if it was possible to determine this.
     */
    @Adjacency(label = COORDINATE, direction = Direction.OUT)
    ArchiveCoordinateModel getCoordinate();

    /**
     * Contains the Maven GAV, if it was possible to determine this.
     */
    @Adjacency(label = COORDINATE, direction = Direction.OUT)
    void setCoordinate(ArchiveCoordinateModel vul);
}
