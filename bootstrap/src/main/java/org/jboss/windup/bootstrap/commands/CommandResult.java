package org.jboss.windup.bootstrap.commands;

/**
 * Result of a {@link Command}.
 * 
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public enum CommandResult
{
    /**
     * Exit windup.
     */
    EXIT,

    /**
     * Continue executing commands.
     */
    CONTINUE;
}
