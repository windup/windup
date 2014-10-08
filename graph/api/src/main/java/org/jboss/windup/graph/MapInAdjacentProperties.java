package org.jboss.windup.graph;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Store Map<String,String> in properties of a vertex.
 *
 * @author Ondrej Zizka
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface MapInAdjacentProperties
{
    /**
     * The edge label for this relationship.
     */
    public String label();
}
