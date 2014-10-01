package com.tinkerpop.frames.structures;

import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.frames.FramedGraph;

import java.util.Iterator;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class FramedVertexIterable<T> implements Iterable<T> {
    protected final Class<T> kind;
    protected final Iterable<Vertex> iterable;
    protected final FramedGraph<? extends Graph> framedGraph;

    public FramedVertexIterable(final FramedGraph<? extends Graph> framedGraph, final Iterable<Vertex> iterable, final Class<T> kind) {
        this.framedGraph = framedGraph;
        this.iterable = iterable;
        this.kind = kind;
    }

    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private Iterator<Vertex> iterator = iterable.iterator();

            public void remove() {
                throw new UnsupportedOperationException();
            }

            public boolean hasNext() {
                return this.iterator.hasNext();
            }

            public T next() {
                return framedGraph.frame(this.iterator.next(), kind);
            }
        };
    }
}
