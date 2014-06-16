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
import org.jboss.windup.config.selectables.SelectionFactory;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.rules.apps.java.scan.model.JavaClassModel;
import org.jboss.windup.rules.apps.java.scan.model.JavaMethodModel;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 * 
 */
public class JavaExampleConfigurationProvider extends WindupConfigurationProvider
{
    private static final Logger LOG = LoggerFactory.getLogger(JavaExampleConfigurationProvider.class);

    private final List<JavaMethodModel> results = new ArrayList<>();

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

                    /*
                     * Specify a set of conditions that must be met in order for the .perform() clause of this rule to
                     * be evaluated.
                     */
                    .when(
                                /*
                                 * Select all java classes with the FQCN matching "com.example.(.*)", store the
                                 * resultant list in a parameter named "javaClasses"
                                 */
                                GraphSearchConditionBuilder
                                            .create("javaClasses")
                                            .ofType(JavaClassModel.class)
                                            .withProperty("qualifiedName", GraphSearchPropertyComparisonType.REGEX,
                                                        "com\\.example\\..*")

                    )

                    /*
                     * If all conditions of the .when() clause were satisfied, the following conditions will be
                     * evaluated
                     */
                    .perform(Iteration.over("javaClasses").queryFor(JavaMethodModel.class, "javaMethod")
                                .out("javaMethod").has("methodName", "toString")
                                .endQuery()
                                .perform(new GraphOperation()
                                {
                                    @Override
                                    public void perform(GraphRewrite event, EvaluationContext context)
                                    {
                                        SelectionFactory selection = SelectionFactory.instance(event);
                                        JavaMethodModel methodModel = selection.getCurrentPayload(
                                                    JavaMethodModel.class, "javaMethod");
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
