package org.jboss.windup.engine;

import org.jboss.windup.engine.WindupProgressMonitor;


/*
 * Null object pattern, presents a no-op implementation to avoid
 * adding cyclomatic complexity to underlying implementation.
 */
class NullWindupProgressMonitor implements WindupProgressMonitor {


    @Override
    public void beginTask(String name, int totalWork)
    {
    }


    @Override
    public void done()
    {
    }


    @Override
    public boolean isCancelled()
    {
        return false;
    }


    @Override
    public void setCancelled(boolean value)
    {
    }


    @Override
    public void setTaskName(String name)
    {
    }


    @Override
    public void subTask(String name)
    {
    }


    @Override
    public void worked(int work)
    {
    }

}// class
