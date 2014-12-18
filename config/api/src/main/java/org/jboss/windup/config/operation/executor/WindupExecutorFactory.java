package org.jboss.windup.config.operation.executor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.jboss.windup.config.GraphRewrite;

public class WindupExecutorFactory
{
    private static final String PERSISTENCE_EXECUTOR_KEY = "windup.persistence.executorservice";

    public static synchronized ExecutorService getSingleThreadedIterationPersistenceExecutor(GraphRewrite event)
    {
        ExecutorService executor = (ExecutorService) event.getRewriteContext().get(PERSISTENCE_EXECUTOR_KEY);
        if (executor == null)
        {
            executor = Executors.newSingleThreadExecutor();
            event.getRewriteContext().put(PERSISTENCE_EXECUTOR_KEY, executor);
        }
        return executor;
    }

    public static synchronized void removeSingleThreadedIterationPersistenceExecutor(GraphRewrite event)
    {
        event.getRewriteContext().put(PERSISTENCE_EXECUTOR_KEY, null);
    }

    public static ExecutorService createExecutorService()
    {
        return createExecutorService(getDefaultThreadCount());
    }

    public static ExecutorService createExecutorService(int threads)
    {
        if (threads == 0)
        {
            threads = getDefaultThreadCount();
        }
        return Executors.newFixedThreadPool(threads);
    }

    private static int getDefaultThreadCount()
    {
        int totalCores = Runtime.getRuntime().availableProcessors();
        int threads = totalCores;
        if (threads == 0)
        {
            threads = 1;
        }
        return threads;
    }
}
