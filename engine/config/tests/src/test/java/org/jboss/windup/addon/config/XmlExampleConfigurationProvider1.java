/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.windup.addon.config;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jboss.windup.addon.config.graphsearch.GraphSearchConditionBuilder;
import org.jboss.windup.addon.config.operation.GraphOperation;
import org.jboss.windup.addon.config.operation.Iteration;
import org.jboss.windup.addon.config.selectables.SelectionFactory;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.meta.xml.XmlMetaFacetModel;
import org.junit.Assert;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 * 
 */
public class XmlExampleConfigurationProvider1 extends WindupConfigurationProvider
{
    final List<XmlMetaFacetModel> typeSearchResults = new ArrayList<>();
    final Set<String> xmlRootNames = new HashSet<>();

    @Override
    public Configuration getConfiguration(GraphContext context)
    {
        Configuration configuration = ConfigurationBuilder.begin()
                    .addRule()
                    .when(GraphSearchConditionBuilder.create("xmlModels").as(XmlMetaFacetModel.class))
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
                                        if (xmlRootNames.contains(xmlFacetModel.getRootTagName()))
                                        {
                                            Assert.fail("Tag found multiple times");
                                        }
                                        xmlRootNames.add(xmlFacetModel.getRootTagName());
                                    }
                                })
                    );
        return configuration;
    }

    public List<XmlMetaFacetModel> getTypeSearchResults()
    {
        return typeSearchResults;
    }

    public Set<String> getXmlRootNames()
    {
        return xmlRootNames;
    }

}
