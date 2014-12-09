package org.jboss.windup.exec.configuration.options;

import org.jboss.windup.config.AbstractWindupConfigurationOption;
import org.jboss.windup.config.InputType;
import org.jboss.windup.config.ValidationResult;

/**
 * Indicates that Windup should overwrite the output directory if it already exists (without prompting).
 * 
 * @author jsightler
 *
 */
public class OverwriteOption extends AbstractWindupConfigurationOption
{
    public static final String NAME = "overwrite";

    @Override
    public String getDescription()
    {
        return "Force overwrite of the output directory, without prompting";
    }

    @Override
    public String getName()
    {
        return NAME;
    }

    @Override
    public String getLabel()
    {
        return "Overwrite";
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
