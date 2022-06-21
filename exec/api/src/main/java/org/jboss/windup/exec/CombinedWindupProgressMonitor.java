package org.jboss.windup.exec;

import java.util.ArrayList;
import java.util.List;

/**
 * Passes the calls to all nested monitors.
 *
 * @author <a href="mailto:zizka@seznam.cz">Ondrej Zizka</a>
 */
public class CombinedWindupProgressMonitor implements WindupProgressMonitor {

    private List<WindupProgressMonitor> monitors = new ArrayList<>();

    public CombinedWindupProgressMonitor addMonitor(WindupProgressMonitor monitor) {
        this.monitors.add(monitor);
        return this;
    }


    @Override
    public void beginTask(String name, int totalWork) {
        monitors.forEach(m -> m.beginTask(name, totalWork));
    }

    @Override
    public void done() {
        monitors.forEach(m -> m.done());
    }

    @Override
    public boolean isCancelled() {
        return monitors.stream().anyMatch(m -> m.isCancelled());
    }

    @Override
    public void setCancelled(boolean value) {
        monitors.forEach(m -> m.setCancelled(value));
    }

    @Override
    public void setTaskName(String name) {
        monitors.forEach(m -> m.setTaskName(name));
    }

    @Override
    public void subTask(String name) {
        monitors.forEach(m -> m.subTask(name));
    }

    @Override
    public void worked(int work) {
        monitors.forEach(m -> m.worked(work));
    }

}
