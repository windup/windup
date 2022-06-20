package org.jboss.windup.graph;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Store Map<String,String> in properties of a vertex.
 *
 * @author <a href="mailto:ozizka@redhat.com">Ondrej Zizka</a>
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface MapInAdjacentProperties {
    /**
     * The edge label for this relationship.
     */
    public String label();
}
