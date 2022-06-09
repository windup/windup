package org.jboss.windup.rules.apps.diva;

import org.jboss.windup.config.AbstractConfigurationOption;
import org.jboss.windup.config.InputType;
import org.jboss.windup.config.ValidationResult;

/**
 *
 * Indicates that the Tattletale report should be generated.
 *
 */
public class EnableTransactionAnalysisOption extends AbstractConfigurationOption
{
    public static final String NAME = "enableTransactionAnalysis";

    @Override
    public String getDescription()
    {
        return "If set, a Transactions report will be generated for each application.";
    }

    @Override
    public String getName()
    {
        return NAME;
    }

    @Override
    public String getLabel()
    {
        return "Should Windup generate a Transactions report?";
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
