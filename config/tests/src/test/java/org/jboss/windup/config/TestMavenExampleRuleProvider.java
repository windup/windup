/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.windup.config;

import java.util.LinkedList;
import java.util.List;

import org.jboss.windup.config.graph.TypeOperation;
import org.jboss.windup.config.model.TestXmlMetaFacetModel;
import org.jboss.windup.config.operation.GraphOperation;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.config.phase.Discovery;
import org.jboss.windup.config.phase.RulePhase;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.rules.apps.java.model.project.MavenProjectModel;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 * 
 */
public class TestMavenExampleRuleProvider extends WindupRuleProvider
{
    private final List<MavenProjectModel> results = new LinkedList<>();

    @Override
    public Class<? extends RulePhase> getPhase()
    {
        return Discovery.class;
    }

    // @formatter:off
    @Override
    public Configuration getConfiguration(GraphContext context)
    {
        Configuration configuration = ConfigurationBuilder.begin()

        // Add the MavenFacetModel type to all XmlMetaFacetModel vertices.
        .addRule()
        .when(
            Query.fromType(TestXmlMetaFacetModel.class)
        )
        .perform(
            Iteration.over(TestXmlMetaFacetModel.class)
            .perform(
                TypeOperation.addType(Iteration.DEFAULT_SINGLE_VARIABLE_STRING, MavenProjectModel.class)
            )
            .endIteration()
        )

        // Add all MavenFacetModel vertices to this.results.
        .addRule()
        .when(
            Query.fromType(MavenProjectModel.class).as("mavenModels")
        )
        .perform(
            Iteration.over(MavenProjectModel.class, "mavenModels")
                .perform( new GraphOperation()
                {
                    @Override
                    public void perform(GraphRewrite event, EvaluationContext context)
                    {
                        Variables varStack = Variables.instance(event);
                        MavenProjectModel mavenFacetModel = 
                            Iteration.getCurrentPayload(varStack, MavenProjectModel.class, Iteration.singleVariableIterationName("mavenModels"));
                        results.add(mavenFacetModel);
                    }
                })
                .endIteration()
        );
        return configuration;
    }
    // @formatter:on

    public List<MavenProjectModel> getSearchResults()
    {
        return results;
    }
}
