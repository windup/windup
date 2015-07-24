package org.jboss.windup.exec.configuration.options;

import org.jboss.windup.config.AbstractConfigurationOption;
import org.jboss.windup.config.InputType;
import org.jboss.windup.config.ValidationResult;

/**
 * Updates the rulesets distributed with Windup.
 * That means, Windup will check the Maven repository for releases of windup-rulesets,
 * and if newer is available, it it downloaded and it replaces the current one.
 */
public class UpdateRulesetsOption extends AbstractConfigurationOption
{
    public static final String NAME = "updateRulesets";

    @Override
    public String getDescription()
    {
        return "Updates the rulesets distributed with Windup.";
    }

    @Override
    public String getName()
    {
        return NAME;
    }

    @Override
    public String getLabel()
    {
        return "Update rulesets";
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
