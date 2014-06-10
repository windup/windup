package org.jboss.windup.config.loader;

import java.util.List;

import org.jboss.windup.config.WindupConfigurationProvider;

/**
 * Each configuration extension will implement this interface to provide a list of WindupConfigurationProviders. A
 * GraphConfigurationLoader will pull in all WindupConfigurationProviders and sort them based upon the provider's
 * metadata.
 * 
 * @author jsightler
 * 
 */
public interface WindupConfigurationProviderLoader
{
    /**
     * Return all WindupConfigurationProviders that are relevant for this loader.
     * 
     * @return
     */
    public List<WindupConfigurationProvider> getProviders();
}
