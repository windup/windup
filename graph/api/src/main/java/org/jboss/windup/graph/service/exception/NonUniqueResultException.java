package org.jboss.windup.graph.service.exception;

/**
 * Thrown when a unique result was expected, but more than one result was found.
 * 
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 *
 */
public class NonUniqueResultException extends RuntimeException
{
    private static final long serialVersionUID = 6490597311405481149L;

    /**
     * Create a new instance of {@link NonUniqueResultException}
     */
    public NonUniqueResultException(String message)
    {
        super(message);
    }
}
