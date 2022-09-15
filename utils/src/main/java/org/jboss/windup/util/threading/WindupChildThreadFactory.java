package org.jboss.windup.util.threading;

import java.util.concurrent.ThreadFactory;

/**
 * A thread-parent thread factory. Thread-parents are useful to distinguish threads that were created from the given windup run.
 */
public class WindupChildThreadFactory implements ThreadFactory {

    private final Thread mainWindupThread;

    public WindupChildThreadFactory() {
        this.mainWindupThread = Thread.currentThread();
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread t = new WindupChildThread(mainWindupThread, r);
        t.setDaemon(true);
        return t;
    }
}
