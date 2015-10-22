package org.jboss.windup.util.threading;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * An alterantive to {@link Executors} that pushes windup specific threadFactory inside by default. This is useful in order to have
 * be able to connect sibling threads into the main thread of windup run.
 */
public class WindupExecutors
{
    public static ExecutorService newFixedThreadPool(int numberOfThreads) {
        return Executors.newFixedThreadPool(numberOfThreads,new WindupChildThreadFactory());
    }

    public static ExecutorService newSingleThreadExecutor() {
        return Executors.newSingleThreadExecutor(new WindupChildThreadFactory());
    }

}
