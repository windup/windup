/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.windup.config;

import javax.inject.Inject;

import org.jboss.windup.config.loader.GraphConfigurationLoader;
import org.jboss.windup.config.runner.DefaultEvaluationContext;
import org.jboss.windup.config.selectables.VarStack;
import org.jboss.windup.graph.GraphContext;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.param.DefaultParameterValueStore;
import org.ocpsoft.rewrite.param.ParameterValueStore;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public class ConfigurationProcessorImpl
{
    @Inject
    private VarStack selectionFactory;

    @Inject
    private GraphConfigurationLoader graphConfigurationLoader;

    public void run(GraphContext context)
    {
        final Configuration configuration = graphConfigurationLoader.loadConfiguration(context);

        final DefaultEvaluationContext evaluationContext = new DefaultEvaluationContext();

        final DefaultParameterValueStore values = new DefaultParameterValueStore();
        evaluationContext.put(ParameterValueStore.class, values);
        GraphRewrite event = new GraphRewrite(context);
        event.getRewriteContext().put(VarStack.class, selectionFactory);

        GraphSubset.evaluate(configuration).perform(event, evaluationContext);
    }
}
