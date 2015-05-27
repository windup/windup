package org.jboss.windup.config;

/**
 * Indicates the result of a validation operation.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 *
 */
public class ValidationResult
{
    /**
     * Indicates that the validation was successful (with no attached message).
     */
    public static final ValidationResult SUCCESS = new ValidationResult(Level.SUCCESS, null);

    public static enum Level
    {
        ERROR, PROMPT_TO_CONTINUE, WARNING, SUCCESS
    };

    private final Level level;
    private final String message;

    /**
     * Indicates the success of failure of a validation, as well as a short informative message for the user.
     */
    public ValidationResult(Level level, String message)
    {
        this.level = level;
        this.message = message;
    }

    public boolean isSuccess()
    {
        return !Level.ERROR.equals(level);
    }

    public Level getLevel()
    {
        return level;
    }

    public String getMessage()
    {
        return message;
    }

    @Override
    public String toString()
    {
        return getMessage();
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((level == null) ? 0 : level.hashCode());
        result = prime * result + ((message == null) ? 0 : message.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ValidationResult other = (ValidationResult) obj;
        if (level != other.level)
            return false;
        if (message == null)
        {
            if (other.message != null)
                return false;
        }
        else if (!message.equals(other.message))
            return false;
        return true;
    }

}
