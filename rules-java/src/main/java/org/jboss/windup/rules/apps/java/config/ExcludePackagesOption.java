package org.jboss.windup.rules.apps.java.config;

import org.jboss.windup.config.AbstractWindupConfigurationOption;
import org.jboss.windup.config.InputType;
import org.jboss.windup.config.ValidationResult;

public class ExcludePackagesOption extends AbstractWindupConfigurationOption
{

    public static final String NAME = "excludePackages";

    @Override
    public String getDescription()
    {
        return "A list of java package name prefixes to exclude (eg, com.myapp.subpackage)";
    }

    @Override
    public String getName()
    {
        return NAME;
    }

    @Override
    public String getLabel()
    {
        return "Exclude Java Packages";
    }

    @Override
    public Class<?> getType()
    {
        return String.class;
    }

    @Override
    public InputType getUIType()
    {
        return InputType.MANY;
    }

    @Override
    public boolean isRequired()
    {
        return false;
    }

    public ValidationResult validate(Object value)
    {
        return ValidationResult.SUCCESS;
    }
}
