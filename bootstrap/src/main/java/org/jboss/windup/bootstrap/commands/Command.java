package org.jboss.windup.bootstrap.commands;

import org.jboss.windup.bootstrap.Bootstrap;

/**
 * A command to be run by the Windup {@link Bootstrap} application.
 * 
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public interface Command
{
    /**
     * Execute this {@link Command} and return the {@link CommandResult}.
     */
    CommandResult execute();

    /**
     * The {@link CommandPhase} this {@link Command} should be executed in.
     */
    CommandPhase getPhase();
}
