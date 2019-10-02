package org.jboss.windup.exec.configuration.options;

import org.jboss.windup.config.AbstractPathConfigurationOption;
import org.jboss.windup.config.InputType;

import java.io.File;

/**
 * Indicates the file that will contain custom labels provided by the user.
 * Multiple paths can be specified separated by a space (for example, --userLabelsDirectory PATH_1 PATH_2).
 *
 * @author <a href="mailto:carlosthe19916@gmail.com">Carlos Feria</a>
 *
 */
public class UserLabelsDirectoryOption extends AbstractPathConfigurationOption
{
    public static final String NAME = "userLabelsDirectory";

    public UserLabelsDirectoryOption()
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
        return "User Metadata Directory";
    }

    @Override
    public String getDescription()
    {
        return "User Labels Directory (Search pattern: *.windup.label.xml, *.rhamt.label.xml). Multiple paths can be specified separated by a space (for example, --userLabelsDirectory PATH_1 PATH_2).";
    }

    @Override
    public Class<?> getType()
    {
        return File.class;
    }

    @Override
    public InputType getUIType()
    {
        return InputType.MANY;
    }

    @Override
    public boolean isRequired()
    {
        return false;
    }

    @Override
    public int getPriority()
    {
        return 7500;
    }
}
