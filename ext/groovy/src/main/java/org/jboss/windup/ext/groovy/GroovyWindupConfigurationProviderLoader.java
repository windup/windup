package org.jboss.windup.ext.groovy;

import groovy.lang.Binding;
import groovy.lang.Closure;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyShell;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.ImportCustomizer;
import org.jboss.forge.furnace.Furnace;
import org.jboss.forge.furnace.addons.Addon;
import org.jboss.forge.furnace.addons.AddonFilter;
import org.jboss.windup.config.WindupConfigurationProvider;
import org.jboss.windup.config.loader.WindupConfigurationProviderLoader;
import org.jboss.windup.ext.groovy.builder.WindupConfigurationProviderBuilder;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.util.FurnaceCompositeClassLoader;
import org.jboss.windup.util.exception.WindupException;

public class GroovyWindupConfigurationProviderLoader implements WindupConfigurationProviderLoader
{
    @Inject
    private FurnaceGroovyRuleScanner scanner;
    @Inject
    private Furnace furnace;
    @Inject
    private GroovyDSLSupport groovyDSLSupport;
    @Inject
    private GraphContext graphContext;

    @Override
    public List<WindupConfigurationProvider> getProviders()
    {
        Binding binding = new Binding();
        binding.setVariable("windupConfigurationProviderBuilders", new ArrayList<WindupConfigurationProviderBuilder>());
        binding.setVariable("supportFunctions", new HashMap<>());
        binding.setVariable("graphContext", graphContext);
        binding.setVariable("registerRegexBlackList", new Closure<Void>(this)
        {
            @Override
            public Void call(Object... args)
            {
                String ruleID = (String) args[0];
                String regexPattern = (String) args[1];
                String hint = (String) args[2];

                GroovyDSLSupport.registerInterest(graphContext, ruleID, regexPattern, hint);
                return null;
            }
        });

        CompilerConfiguration config = new CompilerConfiguration();
        config.addCompilationCustomizers(new ImportCustomizer());
        ClassLoader loader = getCompositeClassloader();
        GroovyShell shell = new GroovyShell(new GroovyClassLoader(loader), binding, config);

        try (InputStream supportFuncsIS = getClass().getResourceAsStream(
                    "/org/jboss/windup/addon/groovy/WindupGroovySupportFunctions.groovy"))
        {
            InputStreamReader isr = new InputStreamReader(supportFuncsIS);
            shell.evaluate(isr);
        }
        catch (Exception e)
        {
            throw new WindupException("Failed to load support functions due to: " + e.getMessage(), e);
        }
        Map<String, ?> supportFunctions = (Map<String, ?>) binding.getVariable("supportFunctions");
        for (Map.Entry<String, ?> supportFunctionEntry : supportFunctions.entrySet())
        {
            binding.setVariable(supportFunctionEntry.getKey(), supportFunctionEntry.getValue());
        }
        binding.setVariable("supportFunctions", null);

        for (URL resource : getScripts())
        {
            try (Reader reader = new InputStreamReader(resource.openStream()))
            {
                shell.evaluate(reader);
            }
            catch (Exception e)
            {
                throw new WindupException("Failed to evaluate configuration: ", e);
            }
        }

        List<WindupConfigurationProviderBuilder> builders = (List<WindupConfigurationProviderBuilder>) binding
                    .getVariable("windupConfigurationProviderBuilders");

        List<WindupConfigurationProvider> wcpList = new ArrayList<>(builders.size());
        for (WindupConfigurationProviderBuilder builder : builders)
        {
            wcpList.add(builder.getWindupConfigurationProvider());
        }
        return wcpList;
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
