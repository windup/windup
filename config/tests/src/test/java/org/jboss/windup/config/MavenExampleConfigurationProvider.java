/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.windup.config;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.jboss.windup.config.graphsearch.GraphSearchConditionBuilder;
import org.jboss.windup.config.operation.GraphOperation;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.config.operation.ruleelement.TypeOperation;
import org.jboss.windup.config.selectables.SelectionFactory;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.meta.xml.MavenFacetModel;
import org.jboss.windup.graph.model.meta.xml.XmlMetaFacetModel;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 * 
 */
public class MavenExampleConfigurationProvider extends WindupConfigurationProvider
{
        private final List<MavenFacetModel> results = new LinkedList();

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
            Iteration.over(XmlMetaFacetModel.class, "xmlModels").var("xml")
                .perform(
                        TypeOperation.addType("xml", MavenFacetModel.class)
                )
            .endIteration()
        )

        // Add all MavenFacetModel vertices to this.results.
        .addRule()
        .when(
                GraphSearchConditionBuilder.create("mavenModels").ofType(MavenFacetModel.class)
        )
        .perform(
            Iteration.over(MavenFacetModel.class, "mavenModels").var("maven")
            .perform(new GraphOperation()
            {
                @Override
                public void perform(GraphRewrite event, EvaluationContext context)
                {
                    SelectionFactory factory = SelectionFactory.instance(event);
                    MavenFacetModel mavenFacetModel = factory.getCurrentPayload(MavenFacetModel.class, "maven");
                    results.add(mavenFacetModel);
                }
            })
            .endIteration()
        );
        return configuration;
    }

    public List<MavenFacetModel> getSearchResults()
    {
        return results;
    }
}
