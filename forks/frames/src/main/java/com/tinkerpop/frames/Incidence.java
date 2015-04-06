package com.tinkerpop.frames;

import com.tinkerpop.blueprints.Direction;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Incidences annotate getters and adders to represent a Vertex incident to an Edge.
 *
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Incidence {
    /**
     * The labels of the edges that are incident to the vertex.
     *
     * @return the edge label
     */
    public String label();

    /**
     * The direction of the edges.
     *
     * @return the edge direction
     */
    public Direction direction() default Direction.OUT;
}
