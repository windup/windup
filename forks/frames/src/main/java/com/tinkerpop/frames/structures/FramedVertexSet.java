package com.tinkerpop.frames.structures;

import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.frames.FramedGraph;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class FramedVertexSet<T> implements Set<T> {
    protected final Class<T> kind;
    protected final Set<Vertex> set;
    protected final FramedGraph<? extends Graph> framedGraph;

    public FramedVertexSet(final FramedGraph<? extends Graph> framedGraph, final Set<Vertex> set, final Class<T> kind) {
        this.framedGraph = framedGraph;
        this.set = set;
        this.kind = kind;
    }

    public boolean contains(final Object object) {
        return this.set.contains(object);
    }

    public boolean containsAll(final Collection collection) {
        return this.set.containsAll(collection);
    }

    public boolean add(T t) {
        throw new UnsupportedOperationException();
    }

    public boolean isEmpty() {
        return this.set.isEmpty();
    }

    public boolean addAll(final Collection collection) {
        throw new UnsupportedOperationException();
    }

    public boolean retainAll(final Collection collection) {
        return this.set.retainAll(collection);
    }

    public boolean remove(final Object object) {
        return this.set.remove(object);
    }

    public boolean removeAll(final Collection collection) {
        return this.set.removeAll(collection);
    }

    public void clear() {
        this.set.clear();
    }

    public int size() {
        return this.set.size();
    }

    public Object[] toArray() {
        throw new UnsupportedOperationException();
    }

    public <T> T[] toArray(T[] array) {
        throw new UnsupportedOperationException();
    }

    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private Iterator<Vertex> iterator = set.iterator();

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
