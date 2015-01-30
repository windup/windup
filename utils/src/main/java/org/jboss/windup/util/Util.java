package org.jboss.windup.util;

import java.util.Iterator;


/**
 *
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
public class Util {

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