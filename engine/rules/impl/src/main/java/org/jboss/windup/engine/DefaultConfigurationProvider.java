/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.windup.engine;

import org.jboss.windup.addon.config.GraphRewrite;
import org.jboss.windup.addon.config.WindupConfigurationProvider;
import org.jboss.windup.addon.config.condition.GraphCondition;
import org.jboss.windup.addon.config.operation.GraphOperation;
import org.jboss.windup.graph.GraphContext;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public class DefaultConfigurationProvider extends WindupConfigurationProvider
{

    @Override
    public Configuration getConfiguration(GraphContext context)
    {
        return ConfigurationBuilder.begin()
                    .addRule()
                    .when(new GraphCondition()
                    {
                        @Override
                        public boolean evaluate(GraphRewrite event, EvaluationContext context)
                        {
                            final GraphContext graph = event.getGraphContext();
                            return graph != null;
                        }
                    })
                    .perform(new GraphOperation()
                    {
                        @Override
                        public void perform(GraphRewrite event, EvaluationContext context)
                        {
                            System.out.println("Performed GraphOperation!");
                        }
                    });
    }

}
