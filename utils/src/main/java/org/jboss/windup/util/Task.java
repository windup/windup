package org.jboss.windup.util;

/**
 * A task to be performed.
 * 
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 *
 * @param <RETURN_TYPE>
 */
public abstract class Task<RETURN_TYPE>
{
    /**
     * Perform the task.
     * 
     * @return The result, if any. (May be null.)
     */
    public abstract RETURN_TYPE execute();
}
