package org.jboss.windup.exec;

/**
 * A progress monitor API to allow monitoring of system while analyzing an application and/or generating reports.
 * 
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public interface WindupProgressMonitor
{
    /**
     * Constant indicating an unknown amount of work.
     */
    public final static int UNKNOWN = -1;

    /**
     * Notifies that the main task is beginning. This must only be called once on a given progress monitor instance.
     * 
     * @param name the name (or description) of the main task
     * @param totalWork the total number of work units into which the main task is been subdivided. If the value is
     *            <code>UNKNOWN</code> the implementation is free to indicate progress in a way which doesn't require
     *            the total number of work units in advance.
     */
    public void beginTask(String name, int totalWork);

    /**
     * Notifies that the work is done; that is, either the main task is completed or the user canceled it. This method
     * may be called more than once (implementations should be prepared to handle this case).
     */
    public void done();

    /**
     * Returns whether cancellation of current operation has been requested. Long-running operations should poll to see
     * if cancelation has been requested.
     * 
     * @return <code>true</code> if cancellation has been requested, and <code>false</code> otherwise
     * @see #setCancelled(boolean)
     */
    public boolean isCancelled();

    /**
     * Sets the cancel state to the given value.
     * 
     * @param value <code>true</code> indicates that cancellation has been requested (but not necessarily acknowledged);
     *            <code>false</code> clears this flag
     * @see #isCancelled()
     */
    public void setCancelled(boolean value);

    /**
     * Sets the task name to the given value. This method is used to restore the task label after a nested operation was
     * executed. Normally there is no need for clients to call this method.
     * 
     * @param name the name (or description) of the main task
     * @see #beginTask(java.lang.String, int)
     */
    public void setTaskName(String name);

    /**
     * Notifies that a subtask of the main task is beginning. Subtasks are optional; the main task might not have
     * subtasks.
     * 
     * @param name the name (or description) of the subtask
     */
    public void subTask(String name);

    /**
     * Notifies that a given number of work unit of the main task has been completed. Note that this amount represents
     * an installment, as opposed to a cumulative amount of work done to date.
     * 
     * @param work a non-negative number of work units just completed
     */
    public void worked(int work);
}
