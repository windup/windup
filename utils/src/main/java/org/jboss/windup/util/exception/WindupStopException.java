package org.jboss.windup.util.exception;

/**
 * Thrown in certain situations when we need to stop Windup execution, typically based on external request.
 *
 * @author <a href="mailto:zizka@seznam.cz">Ondrej Zizka</a>
 */
public final class WindupStopException extends WindupException
{
    public WindupStopException(String message)
    {
        super(message);
    }

    public WindupStopException(String message, Throwable cause)
    {
        super(message, cause);
    }
}