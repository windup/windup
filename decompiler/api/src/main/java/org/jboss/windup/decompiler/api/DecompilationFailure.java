package org.jboss.windup.decompiler.api;

import java.util.List;

/**
 * Contains information about a decompilation failure.
 *
 * @author <a href="mailto:ozizka@redhat.com">Ondrej Zizka</a>
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public class DecompilationFailure
{
    private final List<String> paths;
    private final String message;
    private final Throwable cause;

    public DecompilationFailure()
    {
        this.paths = null;
        this.message = null;
        this.cause = null;
    }

    public DecompilationFailure(String message, List<String> path, Throwable cause)
    {
        this.message = message;
        this.cause = cause;
        this.paths = path;
    }

    public List<String> getPath()
    {
        return paths;
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
