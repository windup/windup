package org.jboss.windup.ext.groovy;

import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyShell;

import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.ImportCustomizer;
import org.jboss.forge.furnace.Furnace;
import org.jboss.forge.furnace.addons.Addon;
import org.jboss.forge.furnace.addons.AddonFilter;
import org.jboss.windup.config.WindupConfigurationProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GroovyConfigurationProviderFactory
{
    private static Logger LOG = LoggerFactory.getLogger(GroovyConfigurationProviderFactory.class);

    @Inject
    private FurnaceGroovyRuleScanner scanner;
    @Inject
    private Furnace furnace;

    public List<WindupConfigurationProvider> getGroovyWindupConfigurationProviders()
    {
        Binding binding = new Binding();
        binding.setVariable("configurationProviders", new Integer(2));

        CompilerConfiguration config = new CompilerConfiguration();
        config.addCompilationCustomizers(new ImportCustomizer());
        ClassLoader loader = getCompositeClassloader();
        GroovyShell shell = new GroovyShell(new GroovyClassLoader(loader), binding, config);

        for (URL resource : getScripts())
        {
            try (Reader reader = new InputStreamReader(resource.openStream()))
            {
                shell.evaluate(reader);
            }
            catch (Exception e)
            {
                LOG.error("Error evaluating groovy script: " + resource.getFile() + " due to: " + e.getMessage(), e);
                // throw new WindupException("Failed to evaluate configuration: ", e);
            }
        }

        return null;
    }

    private ClassLoader getCompositeClassloader()
    {
        List<ClassLoader> loaders = new ArrayList<>();
        AddonFilter filter = new AddonFilter()
        {
            @Override
            public boolean accept(Addon addon)
            {
                // TODO this should only accept addons that depend on windup-config-groovy or whatever we call that
                return true;
            }
        };

        for (Addon addon : furnace.getAddonRegistry().getAddons(filter))
        {
            loaders.add(addon.getClassLoader());
        }

        return new FurnaceCompositeClassLoader(getClass().getClassLoader(), loaders);
    }

    private Iterable<URL> getScripts()
    {
        Iterable<URL> scripts = scanner.scan("windup.groovy");
        return scripts;
    }

}
