package org.jboss.windup.graph.model.resource;


import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;
import static org.jboss.windup.graph.model.resource.FileModel.PARENT_DIR;

/**
 * Represents a directory in a filesystem.
 *
 */
@TypeValue(DirectoryModel.TYPE)
public interface DirectoryModel extends PathModel
{
    /**
     * Add a file to this directory
     */
    @Adjacency(label = PARENT_DIR, direction = Direction.IN)
    public void addPathToDirectory(PathModel pathModel);


    /**
     * Files contained within this directory
     *
     * @return
     */
    @Adjacency(label = PARENT_DIR, direction = Direction.IN)
    public Iterable<PathModel> getPathsInDirectory();
}
