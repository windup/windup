package org.jboss.windup.bootstrap.commands;

import org.jboss.windup.bootstrap.Bootstrap;

/**
 * A phase in the {@link Bootstrap} life-cycle.
 * 
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public enum CommandPhase
{
    /**
     * Before Furnace/Windup configuration has begun.
     */
    PRE_CONFIGURATION,

    /**
     * Furnace/Windup is being configured.
     */
    CONFIGURATION,

    /**
     * Furnace/Windup is configured but not started.
     */
    POST_CONFIGURATION,

    /**
     * Furnace is started, Windup has not been executed.
     */
    PRE_EXECUTION,

    /**
     * Windup is executing.
     */
    EXECUTION,

    /**
     * Windup has finished executing.
     */
    POST_EXECUTION
}
