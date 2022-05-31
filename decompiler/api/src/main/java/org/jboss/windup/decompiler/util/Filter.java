package org.jboss.windup.decompiler.util;

/**
 * A filter for anything that can ACCEPT, REJECT, or STOP.
 *
 * @author <a href="mailto:ozizka@redhat.com">Ondrej Zizka</a>
 */
public interface Filter<T> {

    /**
     * Meaning of the returned values: ACCEPT - given object is accepted, filtering continues; REJECT - given object is
     * rejected, filtering continues; STOP - given object is rejected, filtering should stop; ACCEPT_STOP - given object
     * is accepted, filtering should stop.
     */
    public Result decide(T what);

    public enum Result {
        ACCEPT, REJECT, STOP, ACCEPT_STOP
    }

}
