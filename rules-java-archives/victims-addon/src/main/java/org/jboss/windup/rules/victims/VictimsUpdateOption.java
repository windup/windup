package org.jboss.windup.rules.victims;

import org.jboss.windup.config.AbstractConfigurationOption;
import org.jboss.windup.config.InputType;
import org.jboss.windup.config.ValidationResult;

/**
 * Indicates that all operations should function in "Offline" mode (without accessing the internet).
 *
 * @author Ondrej Zizka
 */
public class VictimsUpdateOption extends AbstractConfigurationOption
{
    public static final String NAME = "victimsUpdate";

    @Override
    public String getDescription()
    {
        return "Indicates whether to download new Victims database";
    }

    @Override
    public String getName()
    {
        return NAME;
    }

    @Override
    public String getLabel()
    {
        return "Update Victims database";
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

    @Override
    public ValidationResult validate(Object valueObj)
    {
        return ValidationResult.SUCCESS;
    }


    @Override
    public Object getDefaultValue()
    {
        return true;
    }
}
