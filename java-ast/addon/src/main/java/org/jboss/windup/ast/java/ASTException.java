package org.jboss.windup.ast.java;

/**
 * Thrown due to errors parsing Java source with the {@link ASTReferenceResolver}
 * 
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 *
 */
public class ASTException extends RuntimeException
{
    private static final long serialVersionUID = 1L;

    /**
     * Create an exception.
     */
    public ASTException()
    {
        super();
    }

    /**
     * Create an exception with the given message and cause.
     */
    public ASTException(String message, Throwable cause)
    {
        super(message, cause);
    }

    /**
     * Create an exception with the given message.
     */
    public ASTException(String message)
    {
        super(message);
    }

}
