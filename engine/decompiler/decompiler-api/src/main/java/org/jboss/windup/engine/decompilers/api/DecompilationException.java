package org.jboss.windup.engine.decompilers.api;

/**
 *
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public class DecompilationException extends Exception
{

    private static final long serialVersionUID = -8377473815060311293L;

    public DecompilationException(String message)
    {
        super(message);
    }

    public DecompilationException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public DecompilationException(Throwable cause)
    {
        super(cause);
    }

}// class
