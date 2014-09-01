package org.jboss.windup.decompiler.util;

import java.io.IOException;
import java.util.Collection;


/**
 * Walks a tree structure and calls given WalkerCallback.
 * 
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public interface Walker<T>
{
    public Collection walk(WalkerCallback<T> callback, Collection<T> results) throws IllegalArgumentException, IOException;
}
