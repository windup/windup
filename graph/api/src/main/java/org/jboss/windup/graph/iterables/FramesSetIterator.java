package org.jboss.windup.graph.iterables;

import org.jboss.windup.graph.model.WindupVertexFrame;

import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * An iterator that wraps up another {@link Iterator<WindupVertexFrame>} and does not return duplicates
 */
public class FramesSetIterator<T extends WindupVertexFrame> implements Iterator<T> {
    private final Iterator<T> wrappedIterator;
    private Set<String> ids = new HashSet<>();
    private T nextFrame = null;


    public FramesSetIterator(Iterator<T> wrappedIterator) {
        this.wrappedIterator = wrappedIterator;
        fillInNextFrame();
    }

    @Override
    public boolean hasNext() {
        return nextFrame != null;
    }

    @Override
    public T next() {
        T nextFrame = getAndSetNewFrame();
        if (nextFrame == null) {
            throw new NoSuchElementException();
        } else {
            return nextFrame;
        }
    }

    private T getAndSetNewFrame() {
        T frameToReturn = nextFrame;
        nextFrame = null;
        fillInNextFrame();
        return frameToReturn;
    }

    private void fillInNextFrame() {
        while (wrappedIterator.hasNext() && nextFrame == null) {
            T frame = wrappedIterator.next();
            String frameId = frame.getElement().id().toString();
            if (!ids.contains(frameId)) {
                ids.add(frameId);
                nextFrame = frame;
            }
        }
    }

    @Override
    public void remove() {
        wrappedIterator.remove();
    }

}
