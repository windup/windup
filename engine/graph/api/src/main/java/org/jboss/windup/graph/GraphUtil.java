package org.jboss.windup.graph;

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
     * 
     * This method takes a generic Vertex and casts that type the specified type. 
     * 
     * Sometimes this is required when referencing properties from Frames that reference other frames.
     * 
     * Eg, archiveEntry.getParentResource() will not work, unless the result is cast to "ArchiveResource" via this method.
    */
    public <T> T castToType(Vertex vertex, Class<T> type);
}
