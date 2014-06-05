/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
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
import org.jboss.windup.config.RulePhase;
import org.jboss.windup.config.WindupConfigurationProvider;
import org.jboss.windup.engine.util.exception.WindupException;
import org.jboss.windup.graph.GraphContext;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.config.Rule;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 *
 */
public class GroovyConfigurationProvider extends WindupConfigurationProvider
{
    @Inject
    private FurnaceGroovyRuleScanner scanner;
    @Inject
    private Furnace furnace;

    @Override
    public Configuration getConfiguration(GraphContext context)
    {
        ConfigurationBuilder builder = ConfigurationBuilder.begin();

        /*
         * Bindings can be used to pre-configure syntactical variables, functions, and shortcuts for the groovy script.
         */
        Binding binding = new Binding();
        binding.setVariable("foo", new Integer(2));

        CompilerConfiguration config = new CompilerConfiguration();
        config.addCompilationCustomizers(new ImportCustomizer());
        ClassLoader loader = getCompositeClassloader();
        GroovyShell shell = new GroovyShell(new GroovyClassLoader(loader), binding, config);

        for (URL resource : getScripts())
        {
            try (Reader reader = new InputStreamReader(resource.openStream()))
            {
                @SuppressWarnings("unchecked")
                List<Rule> rules = (List<Rule>) shell.evaluate(reader);
                for (Rule rule : rules)
                {
                    builder.addRule(rule);
                }
            }
            catch (Exception e)
            {
                throw new WindupException("Failed to evaluate configuration: ", e);
            }
        }

        return builder;
    }

    private ClassLoader getCompositeClassloader()
    {
        List<ClassLoader> loaders = new ArrayList<>();
        AddonFilter filter = new AddonFilter()
        {
            @Override
            public boolean accept(Addon addon)
            {
                // TODO this should only accept addons that depend on windup-config addon or whatever we call that
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
        Iterable<URL> scripts = scanner.scan();
        return scripts;
    }

    @Override
    public RulePhase getPhase()
    {
        return RulePhase.COMPOSITION;
    }

}
