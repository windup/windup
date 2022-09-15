package org.jboss.windup.graph.iterables;

import org.jboss.windup.graph.model.WindupVertexFrame;

import java.util.Iterator;

/**
 * An iterable that wraps up another {@link Iterable<WindupVertexFrame>} and does not return duplicates
 */
public class FramesSetIterable<T extends WindupVertexFrame> implements Iterable<T> {
    private final Iterable<? extends WindupVertexFrame> wrappedIterable;

    public FramesSetIterable(Iterable<WindupVertexFrame> wrappedIterable) {
        this.wrappedIterable = wrappedIterable;
    }

    @Override
    public Iterator<T> iterator() {
        return new FramesSetIterator(wrappedIterable.iterator());
    }
}