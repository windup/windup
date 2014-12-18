package org.apache.log4j.helpers;

import java.util.Hashtable;

public final class ThreadLocalMap extends InheritableThreadLocal{
    public final Object childValue(final Object parentValue){
        final Hashtable ht=(Hashtable)parentValue;
        if(ht!=null){
            return ht.clone();
        }
        return null;
    }
}
