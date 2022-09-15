package org.jboss.windup.util;

import org.jboss.windup.util.threading.WindupChildThread;

import java.io.FileWriter;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringEscapeUtils;

/**
 * <p>
 * {@link ExecutionStatistics} provides a simple system for storing the time taken to perform operations.
 * </p>
 *
 * <p>
 * Example usage:
 *
 * <pre>
 * ExecutionStatistics.get().begin(&quot;Process-01&quot;);
 * // ... your code to be timed goes here
 * ExecutionStatistics.get().end(&quot;Process-01&quot;);
 * </pre>
 *
 * </p>
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class ExecutionStatistics {
    private static final Logger LOG = Logging.get(ExecutionStatistics.class);

    private static final Map<Thread, ExecutionStatistics> stats = new ConcurrentHashMap<>();
    private final Map<String, TimingData> executionInfo = new HashMap<>();


    private ExecutionStatistics() {

    }

    /**
     * Gets the instance associated with the current thread.
     */
    public static synchronized ExecutionStatistics get() {
        Thread currentThread = Thread.currentThread();
        if (stats.get(currentThread) == null) {
            stats.put(currentThread, new ExecutionStatistics());
        }
        return stats.get(currentThread);
    }

    public Map<String, TimingData> getExecutionInfo() {
        return executionInfo;
    }

    /**
     * Merge this ExecutionStatistics with all the statistics created within the child threads. All the child threads had to be created using Windup-specific
     * ThreadFactory in order to contain a reference to the parent thread.
     */
    public void merge() {
        Thread currentThread = Thread.currentThread();
        if (!stats.get(currentThread).equals(this) || currentThread instanceof WindupChildThread) {
            throw new IllegalArgumentException("Trying to merge executionstatistics from a "
                    + "different thread that is not registered as main thread of application run");
        }

        for (Thread thread : stats.keySet()) {
            if (thread instanceof WindupChildThread && ((WindupChildThread) thread).getParentThread().equals(currentThread)) {
                merge(stats.get(thread));
            }
        }
    }

    /**
     * Merge two ExecutionStatistics into one. This method is private in order not to be synchronized (merging.
     *
     * @param otherStatistics
     */
    private void merge(ExecutionStatistics otherStatistics) {
        for (String s : otherStatistics.executionInfo.keySet()) {
            TimingData thisStats = this.executionInfo.get(s);
            TimingData otherStats = otherStatistics.executionInfo.get(s);
            if (thisStats == null) {
                this.executionInfo.put(s, otherStats);
            } else {
                thisStats.merge(otherStats);
            }

        }
    }

    /**
     * Clears the current threadlocal as well as any current state.
     */
    public void reset() {
        stats.remove(Thread.currentThread());
        executionInfo.clear();
    }

    /**
     * Serializes the timing data to a "~" delimited file at outputPath.
     */
    public void serializeTimingData(Path outputPath) {
        //merge subThreads instances into the main instance
        merge();

        try (FileWriter fw = new FileWriter(outputPath.toFile())) {
            fw.write("Number Of Executions, Total Milliseconds,  Milliseconds per execution, Type\n");
            for (Map.Entry<String, TimingData> timing : executionInfo.entrySet()) {
                TimingData data = timing.getValue();
                long totalMillis = (data.totalNanos / 1000000);
                double millisPerExecution = (double) totalMillis / (double) data.numberOfExecutions;
                fw.write(String.format("%6d, %6d, %8.2f, %s\n",
                        data.numberOfExecutions, totalMillis, millisPerExecution,
                        StringEscapeUtils.escapeCsv(timing.getKey())
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static <T> T performBenchmarked(String key, Task<T> operation) {
        ExecutionStatistics instance = ExecutionStatistics.get();
        instance.begin(key);
        try {
            return operation.execute();
        } finally {
            instance.end(key);
        }
    }

    /**
     * Start timing an operation with the given identifier.
     */
    public void begin(String key) {
        if (key == null) {
            return;
        }
        TimingData data = executionInfo.get(key);
        if (data == null) {
            data = new TimingData(key);
            executionInfo.put(key, data);
        }
        data.begin();
    }

    /**
     * Complete timing the operation with the given identifier. If you had not previously started a timing operation with this identifier, then this
     * will effectively be a noop.
     */
    public void end(String key) {
        if (key == null) {
            return;
        }
        TimingData data = executionInfo.get(key);
        if (data == null) {
            LOG.info("Called end with key: " + key + " without ever calling begin");
            return;
        }
        data.end();
    }

    public class TimingData {
        private final String key;
        private long startTime;
        private long numberOfExecutions;
        private long totalNanos;

        public TimingData(String key) {
            this.key = key;
        }

        public void begin() {
            this.startTime = System.nanoTime();
        }

        public void end() {
            if (this.startTime == 0) {
                LOG.info("Called end with key: " + this.key + " without ever calling begin");
                return;
            }
            this.totalNanos += (System.nanoTime() - startTime);
            this.startTime = 0;
            this.numberOfExecutions++;
        }

        public void merge(TimingData other) {
            this.numberOfExecutions += other.numberOfExecutions;
            this.totalNanos = other.totalNanos;
        }

        public long getTotal() {
            return totalNanos;
        }
    }
}
