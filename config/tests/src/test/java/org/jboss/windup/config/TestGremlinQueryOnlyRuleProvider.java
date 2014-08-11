package org.jboss.windup.config;

import java.util.ArrayList;
import java.util.List;

import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.config.operation.ruleelement.AbstractIterationOperation;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.config.query.QueryGremlinCriterion;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.rules.apps.java.model.JavaMethodModel;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.Context;
import org.ocpsoft.rewrite.context.EvaluationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thinkaurelius.titan.core.attribute.Text;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.gremlin.java.GremlinPipeline;

public class TestGremlinQueryOnlyRuleProvider extends WindupRuleProvider
{
    private static final Logger LOG = LoggerFactory.getLogger(JavaExampleRuleProvider.class);

    private final List<JavaMethodModel> results = new ArrayList<>();

    @Override
    public RulePhase getPhase()
    {
        return RulePhase.DISCOVERY;
    }

    @Override
    public void enhanceMetadata(Context context)
    {
        context.put(RuleMetadata.CATEGORY, "Java");
    }

    // @formatter:off
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
            Query.gremlin(new QueryGremlinCriterion()
            {
                @Override
                public void query(GraphRewrite event, GremlinPipeline<Vertex, Vertex> pipeline)
                {
                    pipeline.V().has(WindupVertexFrame.TYPE_PROP, Text.CONTAINS, JavaMethodModel.TYPE);
                }
            }).as("javaMethods")
        )

        /*
         * If all conditions of the .when() clause were satisfied, the following conditions will be
         * evaluated
         */
        .perform(
            Iteration.over("javaMethods").as("javaMethod")
            .perform(new AbstractIterationOperation<JavaMethodModel>(JavaMethodModel.class,"javaMethod")
            {
                @Override
                public void perform(GraphRewrite event, EvaluationContext context, JavaMethodModel methodModel)
                {
                    results.add(methodModel);
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
