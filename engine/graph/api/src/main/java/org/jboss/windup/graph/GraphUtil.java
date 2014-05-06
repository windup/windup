package org.jboss.windup.graph;

import org.jboss.windup.graph.model.meta.WindupVertexFrame;

/**
 * Contains various useful methods for dealing with Graph objects
 * 
 * @author jsightler
 * 
 */
public interface GraphUtil
{
    /**
     * Adds the specified type to this frame, and returns a new object that implements this type.
     * 
     * @see GraphTypeManagerTest
     * 
     * @param frame
     * @param type
     * @return
     */
    public <T extends WindupVertexFrame> T addTypeToModel(WindupVertexFrame frame, Class<T> type);
}
