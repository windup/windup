package org.jboss.windup.graph;

import org.jboss.windup.graph.model.meta.WindupVertexFrame;

import com.tinkerpop.blueprints.Vertex;

/**
 * Contains various useful methods for dealing with Graph objects
 * 
 * @author jsightler
 *
 */
public interface GraphUtil
{
    /**
     * Adds the specified type to this frame, and returns a new object
     * that implements this type.
     * 
     * @see GraphTypeManagerTest
     * 
     * @param frame
     * @param type
     * @return
     */
    public <T extends WindupVertexFrame> T addTypeToModel(WindupVertexFrame frame, Class<T> type);
    
    /**
     * 
     * This method takes a generic Vertex and casts that type the specified type. 
     * 
     * Sometimes this is required when referencing properties from Frames that reference other frames.
     * 
     * Eg, archiveEntry.getParentResource() will not work, unless the result is cast to "ArchiveResource" via this method.
    */
    public <T> T castToType(Vertex vertex, Class<T> type);
}
