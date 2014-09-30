package org.jboss.windup.testutil.html;

/**
 * Thrown by the report validations to indicate an unexpected result was found.
 * 
 * @author jsightler <jesse.sightler@gmail.com>
 *
 */
public class CheckFailedException extends RuntimeException
{
    private static final long serialVersionUID = 1L;

    public CheckFailedException(String message)
    {
        super(message);
    }
}
