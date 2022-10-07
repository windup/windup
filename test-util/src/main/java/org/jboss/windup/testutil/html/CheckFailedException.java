package org.jboss.windup.testutil.html;

/**
 * Thrown by the report validations to indicate an unexpected result was found.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class CheckFailedException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public CheckFailedException(String message) {
        super(message);
    }
}
