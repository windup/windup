package org.jboss.windup.graph;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Used to create a relationship as a Map. Can be placed on methods of the following signatures:
 * <p>
 * Map<String, VertexFrame> getMap(); void setMap(Map<String, VertexFrame> mapEntries);
 * <p>
 * setMap will replace all items in the map with the provided values. getMap returns a read-only Map, although the items
 * contained within the map are read-write.
 * <p>
 * The map key is stored within a property of the provided name on the Edge between the two vertices.
 * <p>
 * Example usage:
 * <pre>
 *     {@literal @}MapInAdjacentVertices(label = "fooMap")
 *     void setAnnotationValues(Map<String, FooModel> values);
 *
 *     {@literal @}MapInAdjacentVertices(label = "fooMap")
 *     Map<String, FooModel> getFoos();
 * </pre>
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface MapInAdjacentVertices {
    /**
     * The edge label for this relationship.
     */
    String label();

    /**
     * The property name used to store the key for each map entry.
     */
    String mapKeyField() default "mapKey";
}
