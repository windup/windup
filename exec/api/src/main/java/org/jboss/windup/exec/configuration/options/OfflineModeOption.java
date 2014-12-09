package org.jboss.windup.exec.configuration.options;

import org.jboss.windup.config.AbstractWindupConfigurationOption;
import org.jboss.windup.config.InputType;
import org.jboss.windup.config.ValidationResult;

/**
 * Indicates that all operations should function in "Offline" mode (without accessing the internet).
 */
public class OfflineModeOption extends AbstractWindupConfigurationOption
{
    public static final String NAME = "offline";

    @Override
    public String getDescription()
    {
        return "Indicates whether to fetch information from the internet";
    }

    @Override
    public String getName()
    {
        return NAME;
    }

    @Override
    public String getLabel()
    {
        return "Offline Mode";
    }

    @Override
    public Class<?> getType()
    {
        return Boolean.class;
    }

    @Override
    public InputType getUIType()
    {
        return InputType.SINGLE;
    }

    @Override
    public boolean isRequired()
    {
        return false;
    }

    public ValidationResult validate(Object valueObj)
    {
        return ValidationResult.SUCCESS;
    }
}
