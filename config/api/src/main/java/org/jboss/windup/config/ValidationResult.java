package org.jboss.windup.config;

/**
 * Indicates the result of a validation operation.
 * 
 * @author jsightler <jesse.sightler@gmail.com>
 *
 */
public class ValidationResult
{
    /**
     * Indicates that the validation was successful (with no attached message).
     */
    public static final ValidationResult SUCCESS = new ValidationResult(true, null);

    private boolean success;
    private String message;

    /**
     * Indicates the success of failure of a validation, as well as a short informative message for the user.
     */
    public ValidationResult(boolean success, String message)
    {
        this.success = success;
        this.message = message;
    }

    public boolean isSuccess()
    {
        return success;
    }

    public String getMessage()
    {
        return message;
    }
}
