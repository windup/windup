package org.jboss.windup.util.exception;

/**
 * Root Windup exception to inherit other Windup-specific exceptions from.
 *
 * @author <a href="mailto:ozizka@redhat.com">Ondrej Zizka</a>
 */
public class WindupException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public WindupException() {
    }

    public WindupException(String message) {
        super(message);
    }

    public WindupException(String message, Throwable cause) {
        super(message, cause);
    }

    public WindupException(Throwable cause) {
        super(cause);
    }
}