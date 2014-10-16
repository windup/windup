package org.jboss.windup.decompiler.api;

/**
 * @author Ondrej Zizka, ozizka at redhat.com
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public class DecompilationException extends RuntimeException
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
}
