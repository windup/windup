/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.windup.addon.groovy;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;

import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.List;

import javax.inject.Inject;

import org.jboss.windup.addon.config.RulePhase;
import org.jboss.windup.addon.config.WindupConfigurationProvider;
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

    @Override
    public Configuration getConfiguration(GraphContext context)
    {
        ConfigurationBuilder builder = ConfigurationBuilder.begin();

        /*
         * Bindings can be used to pre-configure syntactical variables, functions, and shortcuts for the groovy script.
         */
        Binding binding = new Binding();
        binding.setVariable("foo", new Integer(2));

        GroovyShell shell = new GroovyShell(binding);

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
