package org.jboss.windup.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Properties;


/**
 *
 *  @author <a href="mailto:ozizka@redhat.com">Ondrej Zizka</a>
 */
public class Util
{
    public static final String NL = System.lineSeparator();

    /**
     * Returns a single item from the Iterator.
     * If there's none, returns null.
     * If there are more, throws an IllegalStateException.
     *
     * @throws IllegalStateException
     */
    public static final <T> T getSingle( Iterable<T> it ) {
        if( ! it.iterator().hasNext() )
            return null;

        final Iterator<T> iterator = it.iterator();
        T o = iterator.next();
        if(iterator.hasNext())
            throw new IllegalStateException("Found multiple items in iterator over " + o.getClass().getName() );

        return o;
    }

}
