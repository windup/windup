package org.jboss.windup.config;

import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.util.Util;

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
        return "If set, "+Util.WINDUP_BRAND_NAME_ACRONYM+" will not delete the temporary working files, like graph database and unzipped archives."
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
        return "Should "+Util.WINDUP_BRAND_NAME_ACRONYM+" keep temporary working directories?";
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
    public ValidationResult validate(Object value)
    {
        return ValidationResult.SUCCESS;
    }

}
