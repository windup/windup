/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.windup.config;

import java.util.ArrayList;
import java.util.List;

import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.config.metadata.MetadataBuilder;
import org.jboss.windup.config.model.TestXmlMetaFacetModel;
import org.jboss.windup.config.operation.GraphOperation;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.config.phase.DiscoveryPhase;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.config.query.QueryPropertyComparisonType;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public class TestXmlExampleRuleProvider2 extends AbstractRuleProvider {
    final List<TestXmlMetaFacetModel> typeSearchResults = new ArrayList<>();

    public TestXmlExampleRuleProvider2() {
        super(MetadataBuilder.forProvider(TestXmlExampleRuleProvider2.class, "TestXmlExampleRuleProvider2")
                .setPhase(DiscoveryPhase.class));
    }

    // @formatter:off
    @Override
    public Configuration getConfiguration(RuleLoaderContext ruleLoaderContext) {
        Configuration configuration = ConfigurationBuilder.begin()
                .addRule()
                .when(Query.fromType(TestXmlMetaFacetModel.class)
                        .withProperty(TestXmlMetaFacetModel.PROPERTY_ROOT_TAG_NAME,
                                QueryPropertyComparisonType.EQUALS, "xmlTag3"))
                .perform(
                        Iteration.over(TestXmlMetaFacetModel.class)
                                .perform(new GraphOperation() {
                                    @Override
                                    public void perform(GraphRewrite event, EvaluationContext context) {
                                        Variables varStack = org.jboss.windup.config.Variables.instance(event);
                                        TestXmlMetaFacetModel xmlFacetModel =
                                                Iteration.getCurrentPayload(varStack, TestXmlMetaFacetModel.class, Iteration.DEFAULT_SINGLE_VARIABLE_STRING);
                                        typeSearchResults.add(xmlFacetModel);
                                    }
                                })
                                .endIteration()
                );
        return configuration;
    }
    // @formatter:on

    public List<TestXmlMetaFacetModel> getTypeSearchResults() {
        return typeSearchResults;
    }

}
