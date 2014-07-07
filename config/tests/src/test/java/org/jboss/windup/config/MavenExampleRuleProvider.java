/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.windup.config;

import java.util.LinkedList;
import java.util.List;
import org.jboss.windup.config.graphsearch.GraphSearchConditionBuilder;
import org.jboss.windup.config.operation.GraphOperation;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.config.operation.ruleelement.TypeOperation;
import org.jboss.windup.config.selectables.VarStack;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.meta.xml.XmlMetaFacetModel;
import org.jboss.windup.rules.apps.maven.model.MavenProjectModel;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 * 
 */
public class MavenExampleRuleProvider extends WindupRuleProvider
{
        private final List<MavenProjectModel> results = new LinkedList();

    @Override
    public RulePhase getPhase()
    {
        return RulePhase.DISCOVERY;
    }

    @Override
    public Configuration getConfiguration(GraphContext context)
    {
        Configuration configuration = ConfigurationBuilder.begin()

        // Add the MavenFacetModel type to all XmlMetaFacetModel vertices.
        .addRule()
        .when(
            GraphSearchConditionBuilder.create("xmlModels").ofType(XmlMetaFacetModel.class)
        )
        .perform(
            Iteration.over(XmlMetaFacetModel.class, "xmlModels").as("xml")
                .perform(
                    TypeOperation.addType("xml", MavenProjectModel.class)
                )
            .endIteration()
        )

        // Add all MavenFacetModel vertices to this.results.
        .addRule()
        .when(
                GraphSearchConditionBuilder.create("mavenModels").ofType(MavenProjectModel.class)
        )
        .perform(
            Iteration.over(MavenProjectModel.class, "mavenModels").as("maven")
            .perform(new GraphOperation()
            {
                @Override
                public void perform(GraphRewrite event, EvaluationContext context)
                {
                    VarStack varStack = VarStack.instance(event);
                    MavenProjectModel mavenFacetModel = varStack.getCurrentPayload(MavenProjectModel.class, "maven");
                    results.add(mavenFacetModel);
                }
            })
            .endIteration()
        );
        return configuration;
    }

    public List<MavenProjectModel> getSearchResults()
    {
        return results;
    }
}
