package org.jboss.windup.rules.apps.openrewrite;

import org.jboss.windup.config.AbstractConfigurationOption;
import org.jboss.windup.config.InputType;
import org.jboss.windup.config.ValidationResult;

/**
 *
 * Indicates that the Openrewrite auto-migration should be used.
 *
 * */
public class EnableOpenrewriteOption extends AbstractConfigurationOption
{
    public static final String NAME = "enableOpenrewrite";

    @Override
    public String getDescription()
    {
        return "If set, Openrewrite auto-migration recipes will be applied to each application.";
    }

    @Override
    public String getName()
    {
        return NAME;
    }

    @Override
    public String getLabel()
    {
        return "Should Windup use Openrewrite auto-migration?";
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
