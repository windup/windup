package com.tinkerpop.frames;

import com.tinkerpop.blueprints.Direction;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Adjacencies annotate getters and adders to represent a Vertex adjacent to a Vertex.
 *
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Adjacency {
    /**
     * The label of the edges making the adjacency between the vertices.
     *
     * @return the edge label
     */
    public String label();

    /**
     * The edge direction of the adjacency.
     *
     * @return the direction of the edges composing the adjacency
     */
    public Direction direction() default Direction.OUT;
}
