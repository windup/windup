package org.jboss.windup.config;

/**
 * Indicates the Java packages for Windup to scan.
 *
 * @author Ondrej Zizka
 */
public class KeepWorkDirsOption extends AbstractConfigurationOption
{
    public static final String NAME = "keepWorkDir";

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
    public ValidationResult validate(Object value)
    {
        return ValidationResult.SUCCESS;
    }

}
