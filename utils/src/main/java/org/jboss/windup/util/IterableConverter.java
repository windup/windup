package org.jboss.windup.util;

import java.util.Iterator;

/**
 * An Iterable that takes from the other Iterable and converts the items using given method.
 * The primary reason is to prevent memory issues which may arise with larger lists.
 *
 * @author <a href="mailto:zizka@seznam.cz">Ondrej Zizka</a>
 */
public abstract class IterableConverter<TFrom, TTo> implements Iterable<TTo>, Converter<TFrom, TTo>
{
    final Iterable<TFrom> sourceIterable;

    public IterableConverter(Iterable<TFrom> sourceIterable)
    {
        this.sourceIterable = sourceIterable;
    }

    public abstract TTo from(TFrom m);


    @Override
    public Iterator<TTo> iterator()
    {
        return new IteratorBacked<>(sourceIterable.iterator(), this);
    }

    class IteratorBacked<TFromX extends TFrom, TToX extends TTo> implements Iterator<TToX> {

        private final Iterator<TFromX> backIterator;
        private final Converter<TFromX, TToX> converter;

        public IteratorBacked(Iterator<TFromX> backIterator, Converter<TFromX, TToX> converter)
        {
            this.backIterator = backIterator;
            this.converter = converter;
        }

        @Override
        public boolean hasNext()
        {
            return backIterator.hasNext();
        }


        @Override
        public TToX next()
        {
            return converter.from(backIterator.next());
        }

    }

}



