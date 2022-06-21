package org.jboss.windup.tooling;

import java.util.logging.LogRecord;

import org.jboss.windup.tooling.IProgressMonitorAdapter;
import org.jboss.windup.tooling.WindupToolingProgressMonitor;

public class ProgressMonitorAdapter implements IProgressMonitorAdapter, WindupToolingProgressMonitor {
    private IProgressMonitorAdapter delegate;

    public ProgressMonitorAdapter(IProgressMonitorAdapter delegate) {
        this.delegate = delegate;
    }

    @Override
    public void beginTask(String task, int totalWork) {
        delegate.beginTask(task, totalWork);
    }

    @Override
    public void done() {
        delegate.done();
    }

    @Override
    public boolean isCancelled() {
        return delegate.isCancelled();
    }

    @Override
    public void setCancelled(boolean value) {
        delegate.setCancelled(value);
    }

    @Override
    public void setTaskName(String name) {
        delegate.setTaskName(name);
    }

    @Override
    public void subTask(String name) {
        delegate.subTask(name);
    }

    @Override
    public void worked(int work) {
        delegate.worked(work);
    }

    @Override
    public void logMessage(LogRecord logRecord) {
        delegate.logMessage(logRecord);
    }
}