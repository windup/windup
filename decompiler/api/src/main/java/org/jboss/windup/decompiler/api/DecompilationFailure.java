package org.jboss.windup.decompiler.api;

/**
 * Contains information about a decompilation failure.
 *
 * @author <a href="mailto:ozizka@redhat.com">Ondrej Zizka</a>
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public class DecompilationFailure
{
    private final String path;
    private final String message;
    private final Throwable cause;

    public DecompilationFailure()
    {
        this.path = null;
        this.message = null;
        this.cause = null;
    }

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
