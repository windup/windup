package org.jboss.windup.exec.configuration.options;

import org.jboss.windup.config.AbstractPathConfigurationOption;
import org.jboss.windup.config.InputType;

/**
 * An option to specify file/directory that contain the regexes of file names to be ignored.
 *
 * @author <a href="mailto:mbriskar@gmail.com">Matej Briskar</a>
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
        return "A path to the list of path patterns to skip during execution. If it points to a directory, all contained files named '*rhamt-ignore.txt' or '*windup-ignore.txt' will be considered as such list.";
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
