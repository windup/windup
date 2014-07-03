package org.jboss.windup.config.loader;

import java.util.List;

import org.jboss.windup.config.WindupRuleProvider;

/**
 * Each configuration extension will implement this interface to provide a list of WindupRuleProviders. A
 * GraphConfigurationLoader will pull in all WindupRuleProviders and sort them based upon the provider's metadata.
 * 
 * @author jsightler
 * 
 */
public interface WindupRuleProviderLoader
{
    /**
     * Return all WindupRuleProviders that are relevant for this loader.
     * 
     * @return
     */
    public List<WindupRuleProvider> getProviders();
}
