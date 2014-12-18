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
    private boolean reloadNeeded = false;

    public FramedVertexIterable(final FramedGraph<? extends Graph> framedGraph, final Iterable<Vertex> iterable, final Class<T> kind) {
        this.framedGraph = framedGraph;
        this.iterable = iterable;
        this.kind = kind;
        
        this.framedGraph.addWrappedGraphReplacedListener(new FramedGraph.WrappedGraphReplacedListener() {
            @Override
            public void onWrappedGraphReplaced() {
        	reloadNeeded = true;
            }
	});
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
        	Vertex v = this.iterator.next();
        	if (reloadNeeded) {
        	    v = framedGraph.getVertex(v.getId());
        	}
                return framedGraph.frame(v, kind);
            }
        };
    }
}
