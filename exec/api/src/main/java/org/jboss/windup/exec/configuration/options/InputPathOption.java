package org.jboss.windup.exec.configuration.options;

import org.jboss.windup.config.AbstractPathConfigurationOption;
import org.jboss.windup.config.InputType;

/**
 * Specifies the Input path for Windup.
 * 
 * @author jsightler <jesse.sightler@gmail.com>
 *
 */
public class InputPathOption extends AbstractPathConfigurationOption
{
    public static final String NAME = "input";

    public InputPathOption()
    {
        super(true);
    }

    @Override
    public String getName()
    {
        return NAME;
    }

    @Override
    public String getLabel()
    {
        return "Input";
    }

    @Override
    public String getDescription()
    {
        return "Input File or Directory (a Directory is required for source mode)";
    }

    @Override
    public InputType getUIType()
    {
        return InputType.FILE_OR_DIRECTORY;
    }

    @Override
    public boolean isRequired()
    {
        return true;
    }

    @Override
    public int getPriority()
    {
        return 10000;
    }
}
