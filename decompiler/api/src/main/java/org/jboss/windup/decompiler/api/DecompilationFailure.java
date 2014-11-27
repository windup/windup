package org.jboss.windup.decompiler.api;

/**
 * @author Ondrej Zizka, ozizka at redhat.com
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public class DecompilationFailure extends Exception
{
    private final String path;
    private final String message;
    private final Throwable cause;

    public DecompilationFailure(String message, String path, Throwable cause)
    {
        this.message = message;
        this.cause = cause;
        this.path = path;
    }

    public String getPath()
    {
        return path;
    }

    public Throwable getCause()
    {
        return cause;
    }

    public String getMessage()
    {
        return message;
    }

}
