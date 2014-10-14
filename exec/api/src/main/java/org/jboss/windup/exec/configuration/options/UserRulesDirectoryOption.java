package org.jboss.windup.exec.configuration.options;

import org.jboss.windup.config.AbstractPathConfigurationOption;
import org.jboss.windup.config.InputType;

/**
 * Indicates the directory that will contain rules provided by the user.
 * 
 * @author jsightler <jesse.sightler@gmail.com>
 *
 */
public class UserRulesDirectoryOption extends AbstractPathConfigurationOption
{
    public static final String NAME = "userRulesDirectory";

    public UserRulesDirectoryOption()
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
        return "User Rules Directory";
    }

    @Override
    public String getDescription()
    {
        return "User Rules Directory (Search pattern: *.windup.groovy and *.windup.xml)";
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
        return 8000;
    }
}
