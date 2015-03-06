package org.jboss.windup.config;

/**
 * Provides a base class for sharing default functionality between {@link ConfigurationOption}s.
 *
 * @author jsightler <jesse.sightler@gmail.com>
 * @author ozizka
 */
public abstract class AbstractWindupConfigurationOption implements ConfigurationOption
{
    @Override
    public int getPriority()
    {
        return 0;
    }

    @Override
    public Object getDefaultValue()
    {
        if (Boolean.class.isAssignableFrom(this.getType()) || boolean.class.isAssignableFrom(this.getType()))
        {
            return false;
        }
        else
            return null;
    }
}
