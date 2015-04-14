package org.jboss.windup.rules.apps.java.config;

import org.jboss.windup.config.AbstractConfigurationOption;
import org.jboss.windup.config.InputType;
import org.jboss.windup.config.ValidationResult;

/**
 * Indicates the Java packages for Windup to scan.
 * 
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class ScanPackagesOption extends AbstractConfigurationOption
{
    public static final String NAME = "packages";

    @Override
    public String getDescription()
    {
        return "A list of java package name prefixes to scan (eg, com.myapp)";
    }

    @Override
    public String getName()
    {
        return NAME;
    }

    @Override
    public String getLabel()
    {
        return "Scan Java Packages";
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
