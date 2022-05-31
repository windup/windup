package org.jboss.windup.reporting.service;

import org.apache.tinkerpop.gremlin.process.traversal.Traverser;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.Set;
import java.util.function.Predicate;

/**
 * Filters the pipeline based upon a {@link Set} of vertices.
 *
 * @author <a href="http://ondra.zizka.cz/">Ondrej Zizka, zizka@seznam.cz</a>
 */
public final class SetMembersFilter implements Predicate<Traverser<Vertex>> {
    private final Set<Vertex> initialVertices;

    /**
     * Only items that match the provided set of vertices will be passed through the pipeline.
     */
    public SetMembersFilter(Set<Vertex> initialVertices) {
        this.initialVertices = initialVertices;
    }

    @Override
    public boolean test(Traverser<Vertex> vertexTraverser) {
        return initialVertices.contains(vertexTraverser.get());
    }
}
