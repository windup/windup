package org.jboss.windup.config.loader;

import org.jboss.windup.util.exception.WindupException;

public class IncorrectPhaseDependencyException extends WindupException
{
    private static final long serialVersionUID = 1L;

    public IncorrectPhaseDependencyException()
    {
    }

    public IncorrectPhaseDependencyException(String message)
    {
        super(message);
    }
}
