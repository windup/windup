package org.jboss.windup.util;

import org.jboss.windup.util.threading.WindupExecutors;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Testing ExecutionStatistics in a multi-threaded environment
 */
public class ExecutionStatisticsTest {

    public static final String STATISTICS_KEY = "key";

    @Test
    public void testMultiThreadedStatistics() throws InterruptedException {
        final ExecutorService executor = WindupExecutors.newFixedThreadPool(10);
        testExecutorService(executor, 1000);
    }

    @Test
    public void testSingleThreadedStatistics() throws InterruptedException {
        final ExecutorService executor = WindupExecutors.newSingleThreadExecutor();
        testExecutorService(executor, 3000);
    }

    private void testExecutorService(ExecutorService executor, int totalMilis) throws InterruptedException {
        Callable<Void> callable = new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ExecutionStatistics.get().begin(STATISTICS_KEY);
                Thread.sleep(1000);
                ExecutionStatistics.get().end(STATISTICS_KEY);
                return null;
            }
        };
        executor.submit(callable);
        executor.submit(callable);
        executor.submit(callable);
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        ExecutionStatistics.get().merge();
        final Map<String, ExecutionStatistics.TimingData> executionInfo = ExecutionStatistics.get().getExecutionInfo();
        Assert.assertEquals(1, executionInfo.size());
        Assert.assertTrue(nanoToMili(executionInfo.get(STATISTICS_KEY).getTotal()) >= totalMilis);
    }

    private long nanoToMili(long nano) {
        return (nano / 1000) / 1000;
    }
}
