/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.windup.addon.config;

import java.util.ArrayList;
import java.util.List;

import org.jboss.windup.addon.config.graphsearch.GraphSearchConditionBuilder;
import org.jboss.windup.addon.config.graphsearch.GraphSearchPropertyComparisonType;
import org.jboss.windup.addon.config.operation.GraphOperation;
import org.jboss.windup.addon.config.operation.Iteration;
import org.jboss.windup.addon.config.selectables.SelectionFactory;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.meta.xml.XmlMetaFacetModel;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 * 
 */
public class XmlExampleConfigurationProvider3 extends WindupConfigurationProvider
{
    final List<XmlMetaFacetModel> typeSearchResults = new ArrayList<>();

    @Override
    public Configuration getConfiguration(GraphContext context)
    {
        Configuration configuration = ConfigurationBuilder
                    .begin()
                    .addRule()
                    .when(GraphSearchConditionBuilder
                                .create("xmlModels")
                                .has(XmlMetaFacetModel.class)
                                .withProperty(XmlMetaFacetModel.PROPERTY_ROOT_TAG_NAME,
                                            GraphSearchPropertyComparisonType.EQUALS,
                                            "xmlTag2"))
                    .perform(Iteration.over(XmlMetaFacetModel.class, "xmlModels", "xml")
                                .perform(new GraphOperation()
                                {
                                    @Override
                                    public void perform(GraphRewrite event, EvaluationContext context)
                                    {
                                        SelectionFactory factory = SelectionFactory.instance(event);
                                        XmlMetaFacetModel xmlFacetModel = factory
                                                    .getCurrentPayload(XmlMetaFacetModel.class);
                                        typeSearchResults.add(xmlFacetModel);
                                    }
                                })
                    );
        return configuration;
    }

    public List<XmlMetaFacetModel> getTypeSearchResults()
    {
        return typeSearchResults;
    }

}
