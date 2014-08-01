/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.windup.config;

import java.util.ArrayList;
import java.util.List;

import org.jboss.windup.config.model.TestXmlMetaFacetModel;
import org.jboss.windup.config.operation.GraphOperation;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.config.query.QueryPropertyComparisonType;
import org.jboss.windup.graph.GraphContext;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 * 
 */
public class XmlExampleRuleProvider2 extends WindupRuleProvider
{
    final List<TestXmlMetaFacetModel> typeSearchResults = new ArrayList<>();

    @Override
    public RulePhase getPhase()
    {
        return RulePhase.DISCOVERY;
    }

    // @formatter:off
    @Override
    public Configuration getConfiguration(GraphContext context)
    {
        Configuration configuration = ConfigurationBuilder.begin()
        .addRule()
            .when(Query.find(TestXmlMetaFacetModel.class)
                .withProperty(TestXmlMetaFacetModel.PROPERTY_ROOT_TAG_NAME,
                            QueryPropertyComparisonType.EQUALS, "xmlTag3")
                .as("xmlModels"))
            .perform(
                Iteration.over(TestXmlMetaFacetModel.class, "xmlModels")
                    .perform(new GraphOperation()
                    {
                        @Override
                        public void perform(GraphRewrite event, EvaluationContext context)
                        {
                            Variables varStack = org.jboss.windup.config.Variables.instance(event);
                            TestXmlMetaFacetModel xmlFacetModel =
                                Iteration.getCurrentPayload(varStack, TestXmlMetaFacetModel.class, Iteration.singleVariableIterationName("xmlModels"));
                            typeSearchResults.add(xmlFacetModel);
                        }
                    })
                    .endIteration()
            );
        return configuration;
    }
    // @formatter:on

    public List<TestXmlMetaFacetModel> getTypeSearchResults()
    {
        return typeSearchResults;
    }

}
