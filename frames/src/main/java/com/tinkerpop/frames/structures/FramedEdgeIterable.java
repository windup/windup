package com.tinkerpop.frames.structures;

import java.util.Iterator;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.frames.FramedGraph;
import com.tinkerpop.frames.InVertex;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class FramedEdgeIterable<T> implements Iterable<T> {
    protected final Class<T> kind;
    protected final Direction direction;
    protected final Iterable<Edge> iterable;
    protected final FramedGraph<? extends Graph> framedGraph;

    /**
	 * @deprecated Use {@link #FramedEdgeIterable(FramedGraph, Iterable, Class)}, in combination with {@link InVertex} and {@link OutVertex}.
	 */
    public FramedEdgeIterable(final FramedGraph<? extends Graph> framedGraph, final Iterable<Edge> iterable, final Direction direction, final Class<T> kind) {
        this.framedGraph = framedGraph;
        this.iterable = iterable;
        this.kind = kind;
        this.direction = direction;
    }
    
    public FramedEdgeIterable(final FramedGraph<? extends Graph> framedGraph, final Iterable<Edge> iterable, final Class<T> kind) {
        this.framedGraph = framedGraph;
        this.iterable = iterable;
        this.kind = kind;
        this.direction = Direction.OUT;
    }

    public Iterator<T> iterator() {
        return new Iterator<T>() {

            private final Iterator<Edge> iterator = iterable.iterator();

            public void remove() {
                throw new UnsupportedOperationException();
            }

            public boolean hasNext() {
                return this.iterator.hasNext();
            }

            public T next() {
                return framedGraph.frame(this.iterator.next(), direction, kind);
            }
        };
    }
}
