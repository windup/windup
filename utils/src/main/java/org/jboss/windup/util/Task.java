package org.jboss.windup.util;

/**
 * A task to be performed.
 *
 * @param <RETURN_TYPE>
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public interface Task<RETURN_TYPE> {
    /**
     * Perform the task.
     *
     * @return The result, if any. (May be null.)
     */
    RETURN_TYPE execute();
}
