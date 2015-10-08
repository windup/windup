package org.jboss.windup.util.threading;

/**
 * Thread used to hold a reference to parent thread.
 */
public class WindupChildThread extends Thread
{


    final Thread parentThread;

    public WindupChildThread(Thread parentThread, Runnable r) {
        super(r);
        this.parentThread=parentThread;
    }
    public Thread getParentThread()
    {
        return parentThread;
    }
}
