package org.jboss.windup.config;

import org.jboss.windup.graph.model.WindupConfigurationModel;

/**
 * Indicates the Java packages for Windup to scan.
 *
 * @author Ondrej Zizka
 */
public class KeepWorkDirsOption extends AbstractConfigurationOption
{
    public static final String NAME = WindupConfigurationModel.KEEP_WORKING_DIRECTORIES;

    @Override
    public String getDescription()
    {
        return "If set, Windup will not delete the temporary working files, like graph database and unzipped archives."
                + " Debugging purposes.";
    }

    @Override
    public String getName()
    {
        return NAME;
    }

    @Override
    public String getLabel()
    {
        return "Should Windup keep temporary working directories?";
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

    @SuppressWarnings("unchecked")
    @Override
    public ValidationResult validate(Object value)
    {
        return ValidationResult.SUCCESS;
    }

}
