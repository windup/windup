package org.jboss.windup.util.exception;

/**
 * Root Windup exception to inherit other Windup-specific exceptions from.
 * 
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public class WindupException extends RuntimeException
{
    private static final long serialVersionUID = 1L;
    
    public WindupException()
    {
    }

    public WindupException(String message)
    {
        super(message);
    }

    public WindupException(String message, Throwable cause)
    {
        super(message, cause);
    }

}