package org.jboss.windup.rules.apps.java.reporting.rules;

import org.jboss.windup.config.AbstractConfigurationOption;
import org.jboss.windup.config.InputType;
import org.jboss.windup.config.ValidationResult;
import org.jboss.windup.util.ThemeProvider;
import org.jboss.windup.util.Util;

/**
 * Indicates that the compatible files report should be generated.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class EnableCompatibleFilesReportOption extends AbstractConfigurationOption
{
    public static final String NAME = "enableCompatibleFilesReport";

    @Override
    public String getDescription()
    {
        return "If set, " + ThemeProvider.getInstance().getTheme().getBrandNameAcronym() + " will generate a report of 'Compatible Files'. Keep in mind " +
                    "that generating this report may take a long time for large applications.";
    }

    @Override
    public String getName()
    {
        return NAME;
    }

    @Override
    public String getLabel()
    {
        return "Should " + ThemeProvider.getInstance().getTheme().getBrandNameAcronym() + " generate the 'Compatible Files Report'?";
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
