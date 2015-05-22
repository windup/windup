package org.jboss.windup.util;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * This provides a mechanism for tracking the progress through a long running-job as well as an estimated time to completion.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class ProgressEstimate
{
    private long startTime = -1;
    private AtomicInteger worked = new AtomicInteger();
    private int total;

    /**
     * Creates a new {@link ProgressEstimate} for the given number of units of work.
     */
    public ProgressEstimate(int total)
    {
        this.startTime = System.currentTimeMillis();
        this.total = total;
    }

    /**
     * Indicates that the given number of work units have been done.
     */
    public void addWork(int worked)
    {
        this.worked.addAndGet(worked);
    }

    /**
     * Gets the current number of work units done.
     */
    public int getWorked()
    {
        return worked.get();
    }

    /**
     * Gets the total number of work units to be done.
     */
    public int getTotal()
    {
        return total;
    }

    /**
     * Gets the estimated time remaining in milliseconds based upon the total number of work units, the start time, and how many units have been done
     * so far.
     *
     * This should not be called before any work units have been done.
     */
    public long getTimeRemainingInMillis()
    {
        long batchTime = System.currentTimeMillis() - startTime;
        double timePerIteration = (double) batchTime / (double) worked.get();
        return (long) (timePerIteration * (total - worked.get()));
    }
}
