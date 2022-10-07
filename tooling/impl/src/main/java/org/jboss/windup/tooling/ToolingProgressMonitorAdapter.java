package org.jboss.windup.tooling;

import java.rmi.RemoteException;
import java.util.logging.LogRecord;

import org.jboss.windup.exec.WindupProgressMonitor;

/**
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class ToolingProgressMonitorAdapter implements WindupToolingProgressMonitor, WindupProgressMonitor {
    private final WindupToolingProgressMonitor delegate;

    public ToolingProgressMonitorAdapter(WindupToolingProgressMonitor delegate) {
        this.delegate = delegate;
    }

    @Override
    public void logMessage(LogRecord logRecord) {
        try {
            this.delegate.logMessage(logRecord);
        } catch (RemoteException e) {
            log("logMessage", e);
        }
    }

    @Override
    public void beginTask(String name, int totalWork) {
        try {
            this.delegate.beginTask(name, totalWork);
        } catch (RemoteException e) {
            log("beginTask", e);
        }
    }

    @Override
    public void done() {
        try {
            this.delegate.done();
        } catch (RemoteException e) {
            log("done", e);
        }
    }

    @Override
    public boolean isCancelled() {
        try {
            return this.delegate.isCancelled();
        } catch (RemoteException e) {
            log("isCancelled", e);
            return true;
        }
    }

    @Override
    public void setCancelled(boolean value) {
        try {
            this.delegate.setCancelled(value);
        } catch (RemoteException e) {
            log("setCancelled", e);
        }
    }

    @Override
    public void setTaskName(String name) {
        try {
            this.delegate.setTaskName(name);
        } catch (RemoteException e) {
            log("setTaskName", e);
        }
    }

    @Override
    public void subTask(String name) {
        try {
            this.delegate.subTask(name);
        } catch (RemoteException e) {
            log("subTask", e);
        }
    }

    @Override
    public void worked(int work) {
        try {
            this.delegate.worked(work);
        } catch (RemoteException e) {
            log("worked", e);
        }
    }

    private void log(String method, RemoteException e) {
        String msg = String.format("ToolingProgressMonitorAdapter:: Failed on '%s' due to: %s", method, e.getMessage());
        System.out.println(msg);
    }
}
