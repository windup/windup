/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.windup.engine;

import java.nio.file.Path;

import org.jboss.forge.furnace.util.Predicate;
import org.jboss.windup.config.WindupRuleProvider;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public interface WindupProcessor
{
    /**
     * Sets the output directory (directory containing the graph, reporting files, and other data)
     */
    void setOutputDirectory(Path outputDirectory);

    /**
     * Executes Windup according to given configuration.
     */
    void execute(WindupProcessorConfig config);

    /**
     * Executes Windup (including all rules)
     * 
     * @deprecated  Use execute( WindupProcessorConfig ).
     */
    void execute();

    /**
     * Executes Windup (including all rules). Progress will be reported using the given {@link WindupProgressMonitor}
     * 
     * @deprecated  Use execute( WindupProcessorConfig ).
     */
    void execute(WindupProgressMonitor progressMonitor);

    /**
     * Executes only the rules contained in providers that are accepted by the provided ruleProviderFilter.
     * 
     * @deprecated  Use execute( WindupProcessorConfig ).
     */
    void execute(Predicate<WindupRuleProvider> ruleProviderFilter);

    /**
     * Executes only the rules contained in providers that are accepted by the provided {@link Predicate}. Progress will
     * be reported using the given {@link WindupProgressMonitor}.
     * 
     * @deprecated  Use execute( WindupProcessorConfig ).
     */
    void execute(Predicate<WindupRuleProvider> ruleProviderFilter, WindupProgressMonitor progressMonitor);
}
