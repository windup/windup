package org.jboss.windup.ast.java;

/**
 * Thrown due to errors parsing Java source with the {@link ASTProcessor}
 * 
 * @author jsightler
 *
 */
public class ASTException extends RuntimeException
{
    private static final long serialVersionUID = 1L;

    public ASTException()
    {
        super();
    }

    public ASTException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public ASTException(String message)
    {
        super(message);
    }

    public ASTException(Throwable cause)
    {
        super(cause);
    }

}
