/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.windup.engine;

import javax.inject.Inject;

import org.jboss.windup.addon.config.GraphRewrite;
import org.jboss.windup.addon.config.GraphSubset;
import org.jboss.windup.addon.config.runner.DefaultEvaluationContext;
import org.jboss.windup.addon.config.selectables.SelectionFactory;
import org.jboss.windup.graph.GraphContext;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationLoader;
import org.ocpsoft.rewrite.param.DefaultParameterValueStore;
import org.ocpsoft.rewrite.param.ParameterValueStore;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public class ConfigurationProcessorImpl
{
    @Inject
    private SelectionFactory selectionFactory;

    public void run(GraphContext context)
    {
        final ConfigurationLoader loader = ConfigurationLoader.create(context);
        final Configuration configuration = loader.loadConfiguration(context);

        final DefaultEvaluationContext evaluationContext = new DefaultEvaluationContext();

        final DefaultParameterValueStore values = new DefaultParameterValueStore();
        evaluationContext.put(ParameterValueStore.class, values);
        GraphRewrite event = new GraphRewrite(context);
        event.getRewriteContext().put(SelectionFactory.class, selectionFactory);

        GraphSubset.evaluate(configuration).perform(event, evaluationContext);
    }
}
