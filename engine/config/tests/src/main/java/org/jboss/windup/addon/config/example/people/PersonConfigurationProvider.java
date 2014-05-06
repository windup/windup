/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.windup.addon.config.example.people;

import org.jboss.windup.addon.config.GraphRewrite;
import org.jboss.windup.addon.config.WindupConfigurationProvider;
import org.jboss.windup.addon.config.condition.GraphCondition;
import org.jboss.windup.addon.config.example.people.Person.Gender;
import org.jboss.windup.addon.config.operation.GraphOperation;
import org.jboss.windup.addon.config.operation.Iteration;
import org.jboss.windup.addon.config.selectables.Selection;
import org.jboss.windup.graph.GraphContext;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public class PersonConfigurationProvider extends WindupConfigurationProvider
{
    private boolean found = false;

    @Override
    public Configuration getConfiguration(GraphContext context)
    {
        Configuration config = ConfigurationBuilder.begin()

                    .addRule()
                    .when(Selection.ofAll(Person.class, "people").gendered(Gender.MALE))
                    .perform(Iteration.over(Person.class, "people", "person")
                                .when(new GraphCondition()
                                {
                                    @Override
                                    public boolean evaluate(GraphRewrite event, EvaluationContext context)
                                    {
                                        return "Lincoln".equals(Selection.current(Person.class).getName());
                                    }
                                })
                                .perform(new GraphOperation()
                                {
                                    @Override
                                    public void perform(GraphRewrite event, EvaluationContext context)
                                    {
                                        found = true;
                                    }
                                })
                    );

        return config;
    }

    public boolean isPersonFound()
    {
        return found;
    }
}
