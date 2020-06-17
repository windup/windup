package org.jboss.windup.exec.configuration.options;

import java.io.File;

import org.jboss.windup.config.AbstractPathConfigurationOption;
import org.jboss.windup.config.InputType;

/**
 * Indicates the directory or directories that will contain rules provided by the user.
 * Multiple paths can be specified separated by a space (for example, --userRulesDirectory PATH_1 PATH_2).
 * 
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
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
        return "User Metadata Directory";
    }

    @Override
    public String getDescription()
    {
        return "User Rules Directory (Search pattern: *.windup.groovy, *.windup.xml, *.rhamt.groovy, *.rhamt.xml, *.mta.groovy and *.mta.xml). Multiple paths can be specified separated by a space (for example, --userRulesDirectory PATH_1 PATH_2).";
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
        return 8000;
    }
}
