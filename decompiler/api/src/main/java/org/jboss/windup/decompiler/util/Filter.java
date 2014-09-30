package org.jboss.windup.decompiler.util;

/**
 * A filter for anything that can ACCEPT, REJECT, or STOP.
 * 
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public interface Filter<T>
{

    public enum Result
    {
        ACCEPT, REJECT, STOP, ACCEPT_STOP
    }

    /**
     * Meaning of the returned values: ACCEPT - given object is accepted, filtering continues; REJECT - given object is
     * rejected, filtering continues; STOP - given object is rejected, filtering should stop; ACCEPT_STOP - given object
     * is accepted, filtering should stop.
     */
    public Result decide(T what);

}
