package org.jboss.windup.util;

import java.util.Iterator;

/**
 * An Iterable that takes from the other Iterable and converts the items using given method.
 * The primary reason is to prevent memory issues which may arise with larger lists.
 *
 * @author <a href="mailto:zizka@seznam.cz">Ondrej Zizka</a>
 */
public abstract class IterableConverter<TFrom, TTo> implements Iterable<TTo> {
    final Iterable<TFrom> sourceIterable;

    /**
     * Creates a new {@link IterableConverter} from the source {@link Iterable}.
     */
    public IterableConverter(Iterable<TFrom> sourceIterable) {
        this.sourceIterable = sourceIterable;
    }

    /**
     * Implements the conversion from the source type to the destination type.
     */
    public abstract TTo from(TFrom m);

    @Override
    public Iterator<TTo> iterator() {
        return new IteratorBacked(sourceIterable.iterator());
    }

    private class IteratorBacked implements Iterator<TTo> {
        private final Iterator<TFrom> backIterator;

        public IteratorBacked(Iterator<TFrom> backIterator) {
            this.backIterator = backIterator;
        }

        @Override
        public boolean hasNext() {
            return backIterator.hasNext();
        }

        @Override
        public TTo next() {
            return from(backIterator.next());
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("remove");
        }
    }
}
