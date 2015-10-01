package org.jboss.windup.bootstrap;

import java.util.logging.Logger;

import org.jboss.windup.exec.WindupProgressMonitor;

/**
 * Implements basic progress monitoring behavior for the Windup command.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class ConsoleProgressMonitor implements WindupProgressMonitor
{
    private static Logger LOG = Logger.getLogger(ConsoleProgressMonitor.class.getName());

    private int totalWork;
    private int currentWork;
    private boolean cancelled;

    @Override
    public void beginTask(String name, int totalWork)
    {
        this.totalWork = totalWork;

        String message = String.format("[%d/%d] %s", currentWork, totalWork, name);
        System.out.println(message);
        LOG.info(message);
    }

    @Override
    public void done()
    {
    }

    @Override
    public boolean isCancelled()
    {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled)
    {
        this.cancelled = cancelled;
    }

    @Override
    public void setTaskName(String name)
    {
        String message = String.format("[%d/%d] \t", currentWork, totalWork, name);
        System.out.println(message);
        LOG.info(message);
    }

    @Override
    public void subTask(String subTask)
    {
        String message = String.format("[%d/%d] %s", currentWork, totalWork, subTask);
        System.out.println(message);
        LOG.info(message);
    }

    @Override
    public void worked(int work)
    {
        this.currentWork += work;
    }
}
