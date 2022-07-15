package org.jboss.windup.util;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.logging.Logger;

/**
 * Wraps the underlying iterator and returns only the matching items.
 */
public class FilteredIterator<E> implements Iterator<E> {

    private Iterator<? extends E> iterator;
    private final Filter<E> filter;
    private E nextElement;
    private boolean hasNext;

    public FilteredIterator(Iterator<? extends E> underlyingIterator, Filter<E> whatToAccept) {
        this.iterator = underlyingIterator;
        this.filter = whatToAccept;

        iterateToNextMatch();
    }

    @Override
    public boolean hasNext() {
        return hasNext;
    }

    @Override
    public E next() {
        if (!hasNext)
            throw new NoSuchElementException();

        return iterateToNextMatch();
    }

    private E iterateToNextMatch() {
        E oldMatch = nextElement;

        while (iterator.hasNext()) {
            E candidate = iterator.next();
            if (filter.accept(candidate)) {
                hasNext = true;
                nextElement = candidate;
                return oldMatch;
            }
        }

        hasNext = false;
        return oldMatch;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException(FilteredIterator.class.getSimpleName() + " can't remove underlying iterator's items as it may be few steps ahead.");
    }

    public static final class AcceptAllFilter<T> implements Filter<T> {
        public boolean accept(final T item) {
            return true;
        }
    }

    public interface Filter<T> {
        /**
         * @return {@code true} if the element matches the filter, otherwise {@code false}
         */
        boolean accept(T item);
    }
}
