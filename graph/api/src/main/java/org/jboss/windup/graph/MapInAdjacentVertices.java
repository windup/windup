package org.jboss.windup.graph;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Used to create a relationship as a Map. Can be placed on methods of the following signatures:
 * 
 * Map<String, VertexFrame> getMap(); void setMap(Map<String, VertexFrame> mapEntries);
 * 
 * setMap will replace all items in the map with the provided values. getMap returns a read-only Map, although the items
 * contained within the map are read-write.
 * 
 * The map key is stored within a property of the provided name on the Edge between the two vertices.
 * 
 * @author jsightler
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface MapInAdjacentVertices
{
    /**
     * The edge label for this relationship.
     */
    public String label();

    /**
     * The property name used to store the key for each map entry.
     */
    public String mapKeyField() default "mapKey";
}
