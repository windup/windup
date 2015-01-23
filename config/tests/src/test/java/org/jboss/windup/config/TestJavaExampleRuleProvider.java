/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.windup.config;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.config.operation.ruleelement.AbstractIterationOperation;
import org.jboss.windup.config.phase.Discovery;
import org.jboss.windup.config.phase.RulePhase;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.config.query.QueryGremlinCriterion;
import org.jboss.windup.config.query.QueryPropertyComparisonType;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.rules.apps.java.model.JavaClassModel;
import org.jboss.windup.rules.apps.java.model.JavaMethodModel;
import org.jboss.windup.util.Logging;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.Context;
import org.ocpsoft.rewrite.context.EvaluationContext;

import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.gremlin.java.GremlinPipeline;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 *
 */
public class TestJavaExampleRuleProvider extends WindupRuleProvider
{
    private static final Logger LOG = Logging.get(TestJavaExampleRuleProvider.class);

    private final List<JavaMethodModel> results = new ArrayList<>();

    @Override
    public Class<? extends RulePhase> getPhase()
    {
        return Discovery.class;
    }

    @Override
    public void enhanceMetadata(Context context)
    {
        super.enhanceMetadata(context);
        context.put(RuleMetadata.CATEGORY, "Java");
    }

    // @formatter:off
    @Override
    public Configuration getConfiguration(GraphContext context)
    {
        QueryGremlinCriterion methodNameCriterion = new QueryGremlinCriterion()
        {
            @Override
            public void query(GraphRewrite event, GremlinPipeline<Vertex, Vertex> pipeline)
            {
                pipeline.out("javaMethod").has("methodName", "toString");
            }
        };

        Configuration configuration = ConfigurationBuilder
        .begin()
        .addRule()

        /*
         * Specify a set of conditions that must be met in order for the .perform() clause of this rule to
         * be evaluated.
         */
        .when(
            /*
             * Select all java classes with the FQCN matching "com.example.(.*)", store the
             * resultant list in a parameter named "javaClasses"
             */
            Query.fromType(JavaClassModel.class)
                .withProperty("qualifiedName", QueryPropertyComparisonType.REGEX,
                            "com\\.example\\..*").as("javaClasses")
                .and(
                    Query.from("javaClasses").piped(methodNameCriterion).as("javaMethods")
                )
        )

        /*
         * If all conditions of the .when() clause were satisfied, the following conditions will be
         * evaluated
         */
        .perform(
            Iteration.over("javaMethods")
            .perform(new AbstractIterationOperation<JavaMethodModel>()
            {
                @Override
                public void perform(GraphRewrite event, EvaluationContext context, JavaMethodModel methodModel)
                {
                    results.add(methodModel);
                    LOG.info("Overridden " + methodModel.getMethodName() + " Method in type: "
                        + methodModel.getJavaClass().getQualifiedName());
                }
            })
            .endIteration()
        );
        return configuration;
    }

    public List<JavaMethodModel> getResults()
    {
        return results;
    }

}
