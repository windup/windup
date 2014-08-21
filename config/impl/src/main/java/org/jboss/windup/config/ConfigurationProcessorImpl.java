/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.windup.config;

import javax.inject.Inject;

import org.jboss.forge.furnace.util.Predicate;
import org.jboss.windup.config.loader.GraphConfigurationLoader;
import org.jboss.windup.graph.GraphContext;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.param.DefaultParameterValueStore;
import org.ocpsoft.rewrite.param.ParameterValueStore;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public class ConfigurationProcessorImpl implements ConfigurationProcessor
{
    @Inject
    private GraphConfigurationLoader graphConfigurationLoader;

    @Override
    public void run(final GraphContext context)
    {
        final Configuration configuration = graphConfigurationLoader.loadConfiguration(context);
        run(context, configuration);
    }

    @Override
    public void run(final GraphContext context, final Predicate<WindupRuleProvider> ruleProviderFilter)
    {
        final Configuration configuration = graphConfigurationLoader.loadConfiguration(context, ruleProviderFilter);
        run(context, configuration);
    }

    private void run(final GraphContext context, final Configuration configuration)
    {
        GraphRewrite event = new GraphRewrite(context);
        RuleSubset.evaluate(configuration).perform(event, createEvaluationContext());
    }

    private DefaultEvaluationContext createEvaluationContext()
    {
        final DefaultEvaluationContext evaluationContext = new DefaultEvaluationContext();
        final DefaultParameterValueStore values = new DefaultParameterValueStore();
        evaluationContext.put(ParameterValueStore.class, values);
        return evaluationContext;
    }
}
