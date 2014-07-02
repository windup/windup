/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.windup.config;

import java.util.ArrayList;
import java.util.List;

import org.jboss.windup.config.graphsearch.GraphSearchConditionBuilder;
import org.jboss.windup.config.graphsearch.GraphSearchPropertyComparisonType;
import org.jboss.windup.config.operation.GraphOperation;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.config.selectables.VarStack;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.meta.xml.XmlMetaFacetModel;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 * 
 */
public class XmlExampleRuleProvider2 extends WindupRuleProvider
{
    final List<XmlMetaFacetModel> typeSearchResults = new ArrayList<>();

    @Override
    public RulePhase getPhase()
    {
        return RulePhase.DISCOVERY;
    }

    @Override
    public Configuration getConfiguration(GraphContext context)
    {
        Configuration configuration = ConfigurationBuilder
        .begin()
        .addRule()
            .when(GraphSearchConditionBuilder
                .create("xmlModels")
                .withProperty(XmlMetaFacetModel.PROPERTY_ROOT_TAG_NAME,
                            GraphSearchPropertyComparisonType.EQUALS,
                            "xmlTag3"))
            .perform(
                Iteration.over(XmlMetaFacetModel.class, "xmlModels").var("xml")
                .perform(new GraphOperation()
                {
                    @Override
                    public void perform(GraphRewrite event, EvaluationContext context)
                    {
                        VarStack varStack = VarStack.instance(event);
                        XmlMetaFacetModel xmlFacetModel = 
                                varStack.getCurrentPayload(XmlMetaFacetModel.class, "xml");
                        typeSearchResults.add(xmlFacetModel);
                    }
                })
                .endIteration()
            );
        return configuration;
    }

    public List<XmlMetaFacetModel> getTypeSearchResults()
    {
        return typeSearchResults;
    }

}
