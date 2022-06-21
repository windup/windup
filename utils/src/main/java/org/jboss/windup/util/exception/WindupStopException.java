package org.jboss.windup.util.exception;

/**
 * Thrown in certain situations when we need to stop Windup execution, typically based on external request.
 *
 * @author <a href="mailto:zizka@seznam.cz">Ondrej Zizka</a>
 */
public final class WindupStopException extends WindupException {
    public WindupStopException(String message) {
        super(message);
    }

    /**
     * Intended for wrapping another exception meant to stop the execution.
     */
    public WindupStopException(String message, Exception cause) {
        super(message, cause);
    }

    /**
     * Intended for wrapping another exception meant to stop the execution.
     */
    public WindupStopException(Exception cause) {
        super(cause);
    }
}