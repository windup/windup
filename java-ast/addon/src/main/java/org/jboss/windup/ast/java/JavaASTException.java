package org.jboss.windup.ast.java;

/**
 * Thrown due to errors parsing Java source with the {@link JavaASTProcessor}
 * 
 * @author jsightler
 *
 */
public class JavaASTException extends RuntimeException
{
    private static final long serialVersionUID = 1L;

    public JavaASTException()
    {
        super();
    }

    public JavaASTException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public JavaASTException(String message)
    {
        super(message);
    }

    public JavaASTException(Throwable cause)
    {
        super(cause);
    }

}
