package org.jboss.windup.util.exception;

public class MarshallingException extends RuntimeException
{
    private static final long serialVersionUID = 1L;

    public MarshallingException(String message, Throwable t)
    {
        super(message, t);
    }
}
