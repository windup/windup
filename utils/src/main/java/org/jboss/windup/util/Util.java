package org.jboss.windup.util;

import java.util.Iterator;


/**
 *
 *  @author <a href="mailto:ozizka@redhat.com">Ondrej Zizka</a>
 */
public class Util
{
    public static final String WINDUP_BRAND_NAME_LONG = "Red Hat Application Migration Toolkit";
    public static final String WINDUP_BRAND_NAME_ACRONYM = "RHAMT";
    public static final String WINDUP_CLI_NAME = "rhamt-cli";

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
