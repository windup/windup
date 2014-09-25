package org.jboss.windup.config;

/**
 * Provides a base class for sharing default functionality between {@link WindupConfigurationOption}s.
 * 
 * @author jsightler <jesse.sightler@gmail.com>
 *
 */
public abstract class AbstractWindupConfigurationOption implements WindupConfigurationOption
{
    @Override
    public int getPriority()
    {
        return Integer.MIN_VALUE;
    }
}
