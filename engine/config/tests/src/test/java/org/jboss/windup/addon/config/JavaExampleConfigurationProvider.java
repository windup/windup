/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.windup.addon.config;

import org.jboss.windup.addon.config.graphsearch.GraphSearchConditionBuilder;
import org.jboss.windup.addon.config.graphsearch.GraphSearchConditionBuilderGremlin;
import org.jboss.windup.addon.config.graphsearch.GraphSearchGremlinCriterion;
import org.jboss.windup.addon.config.graphsearch.GraphSearchPropertyComparisonType;
import org.jboss.windup.addon.config.operation.GraphOperation;
import org.jboss.windup.addon.config.operation.Iteration;
import org.jboss.windup.addon.config.selectables.SelectionFactory;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.resource.JavaClassModel;
import org.jboss.windup.graph.model.resource.JavaMethodModel;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.gremlin.java.GremlinPipeline;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 * 
 */
public class JavaExampleConfigurationProvider extends WindupConfigurationProvider
{
    private static final Logger LOG = LoggerFactory.getLogger(JavaExampleConfigurationProvider.class);

    @Override
    public Configuration getConfiguration(GraphContext context)
    {

        Configuration configuration = ConfigurationBuilder
                    .begin()
                    .addRule()

                    /*
                     * Specify a set of conditions that must be met in order for the .perform() clause of this rule to
                     * be evaluated.
                     */
                    .when(
                                /*
                                 * Select all java classes with the FQCN matching "javax.(.*)", store the resultant list
                                 * in a parameter named "types"
                                 */
                                GraphSearchConditionBuilder
                                            .create("javaClasses")
                                            .has(JavaClassModel.class)
                                            .withProperty("qualifiedName", GraphSearchPropertyComparisonType.REGEX,
                                                        "javax\\.*")
                    )

                    /*
                     * If all conditions of the .when() clause were satisfied, the following conditions will be
                     * evaluated
                     */
                    .perform(Iteration.query(
                                /*
                                 * Iterate over the list of java types that were selected in the .when() clause. Each
                                 * iteration sets the current PersonFrame into var "type", and into the "current scope"
                                 * for the PersonFrame type.
                                 */
                                GraphSearchConditionBuilderGremlin.create().withCriterion(
                                            new GraphSearchGremlinCriterion()
                                            {
                                                @Override
                                                public void query(GremlinPipeline<Vertex, Vertex> pipeline)
                                                {
                                                    pipeline.out("javaMethod").has("methodName", "toString");
                                                }
                                            }),
                                "javaClasses", "javaClass")
                                .perform(
                                            new GraphOperation()
                                            {

                                                @Override
                                                public void perform(GraphRewrite event,
                                                            EvaluationContext context)
                                                {
                                                    SelectionFactory selection = SelectionFactory
                                                                .instance(event);
                                                    JavaMethodModel methodModel = selection
                                                                .getCurrentPayload(JavaMethodModel.class);
                                                    LOG.info("Overridden "
                                                                + methodModel.getMethodName()
                                                                + " Method in type: "
                                                                + methodModel.getJavaClass()
                                                                            .getQualifiedName());
                                                }
                                            }
                                )
                    );
        return configuration;
    }

}
