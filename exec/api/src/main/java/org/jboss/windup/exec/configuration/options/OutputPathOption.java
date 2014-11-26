package org.jboss.windup.exec.configuration.options;

import org.jboss.windup.config.AbstractPathConfigurationOption;
import org.jboss.windup.config.InputType;

/**
 * Indicates that output path for the windup report and other data produced by a Windup execution.
 * 
 * @author jsightler <jesse.sightler@gmail.com>
 *
 */
public class OutputPathOption extends AbstractPathConfigurationOption
{
    public static final String NAME = "output";

    public OutputPathOption()
    {
        super(false);
    }

    @Override
    public String getName()
    {
        return NAME;
    }

    @Override
    public String getLabel()
    {
        return "Output";
    }

    @Override
    public String getDescription()
    {
        return "Output Directory (WARNING: any existing files will be removed)";
    }

    @Override
    public InputType getUIType()
    {
        return InputType.DIRECTORY;
    }

    @Override
    public boolean isRequired()
    {
        return false;
    }

    @Override
    public int getPriority()
    {
        return 9000;
    }
}
