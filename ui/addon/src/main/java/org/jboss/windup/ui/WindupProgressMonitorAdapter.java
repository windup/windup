package org.jboss.windup.ui;

import org.jboss.forge.addon.ui.progress.UIProgressMonitor;
import org.jboss.windup.exec.WindupProgressMonitor;

/**
 * Progress monitor implementation based on the Forge/Eclipse progress monitor feature.
 * 
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public class WindupProgressMonitorAdapter implements WindupProgressMonitor
{
    private UIProgressMonitor delegate;

    public WindupProgressMonitorAdapter(UIProgressMonitor delegate)
    {
        this.delegate = delegate;
    }

    @Override
    public void beginTask(String name, int totalWork)
    {
        delegate.beginTask(name, totalWork);
    }

    @Override
    public void done()
    {
        delegate.done();
    }

    @Override
    public boolean isCancelled()
    {
        return delegate.isCancelled();
    }

    @Override
    public void setCancelled(boolean value)
    {
        delegate.setCancelled(value);
    }

    @Override
    public void setTaskName(String name)
    {
        delegate.setTaskName(name);
    }

    @Override
    public void subTask(String name)
    {
        delegate.subTask(name);
    }

    @Override
    public void worked(int work)
    {
        delegate.worked(work);
    }

    // TODO make sure progress information is logged.

}
