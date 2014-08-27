package org.jboss.windup.util.exception;

public class XPathException extends RuntimeException
{

    private static final long serialVersionUID = 1L;

    public XPathException(String message, Throwable t)
    {
        super(message, t);
    }
}
