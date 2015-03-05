package org.jboss.windup.test.utils;

import java.util.HashSet;
import java.util.Set;
import org.jboss.forge.furnace.proxy.Proxies;
import org.jboss.forge.furnace.util.Predicate;
import org.jboss.windup.config.WindupRuleProvider;

/**
 *
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
public class EnumeratedRuleProviderFilter implements Predicate<WindupRuleProvider>
{

    // Using strings to prevent inequality of same class from different classloaders.
    private final Set<String> ruleProviders;


    public EnumeratedRuleProviderFilter(Class<? extends WindupRuleProvider>... ruleProviders)
    {
        this.ruleProviders = new HashSet(ruleProviders.length);
        for( Class<? extends WindupRuleProvider> ruleProvider : ruleProviders ){
            if(Proxies.isProxyType(ruleProvider))
                ruleProvider = (Class<? extends WindupRuleProvider>) Proxies.unwrapProxyTypes(ruleProvider.getClass());
            this.ruleProviders.add(ruleProvider.getName());
        }
    }


    @Override
    public boolean accept(WindupRuleProvider ruleProvider)
    {
        Class<? extends WindupRuleProvider> clazz = ruleProvider.getClass();
        if(Proxies.isProxyType(clazz))
            clazz = (Class<? extends WindupRuleProvider>) Proxies.unwrapProxyTypes(clazz);
        return this.ruleProviders.contains(clazz.getName());
    }
}
