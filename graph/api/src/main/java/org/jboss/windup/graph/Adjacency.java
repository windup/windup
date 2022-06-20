package org.jboss.windup.graph;

import org.apache.tinkerpop.gremlin.structure.Direction;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Workaround, until this is merged and released:
 * https://github.com/Syncleus/Ferma/pull/40
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Adjacency {

    /**
     * The label of the edges making the adjacency between the vertices.
     *
     * @return the edge label
     * @since 2.0.0
     */
    String label();

    /**
     * The edge direction of the adjacency.
     *
     * @return the direction of the edges composing the adjacency
     * @since 2.0.0
     */
    Direction direction() default Direction.OUT;

    /**
     * The operation the method is performing on the vertex.
     *
     * @return The operation to be performed.
     */
    org.jboss.windup.graph.Adjacency.Operation operation() default org.jboss.windup.graph.Adjacency.Operation.AUTO;

    enum Operation {
        GET, ADD, REMOVE, SET, AUTO
    }

    ;
}

