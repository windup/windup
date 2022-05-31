package org.jboss.windup.tooling;

import java.util.logging.LogRecord;

public interface IProgressMonitorAdapter {
    void beginTask(String task, int totalWork);

    void done();

    boolean isCancelled();

    void setCancelled(boolean value);

    void setTaskName(String name);

    void subTask(String name);

    void worked(int work);

    void logMessage(LogRecord logRecord);
}