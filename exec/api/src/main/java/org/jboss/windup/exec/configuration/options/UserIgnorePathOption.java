package org.jboss.windup.exec.configuration.options;

import org.jboss.windup.config.AbstractPathConfigurationOption;
import org.jboss.windup.config.InputType;

/**
 * An option to specify file/directory that contain the regexes of file names to be ignored.
 * @author mbriskar
 *
 */
public class UserIgnorePathOption extends AbstractPathConfigurationOption
{

    public static final String NAME = "userIgnorePath";

    public UserIgnorePathOption()
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
        return "User Ignore Path";
    }

    @Override
    public String getDescription()
    {
        return "User Ignore Path. In case of directory it rakes all the files inside matching the pattern. (Search pattern: *windup-ignore.txt)";
    }

    @Override
    public InputType getUIType()
    {
        return InputType.FILE_OR_DIRECTORY;
    }

    @Override
    public boolean isRequired()
    {
        return false;
    }

    @Override
    public int getPriority()
    {
        return 7000;
    }
}
