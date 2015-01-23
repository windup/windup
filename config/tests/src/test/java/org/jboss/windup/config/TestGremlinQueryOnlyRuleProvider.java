package org.jboss.windup.config;

import java.util.ArrayList;
import java.util.List;

import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.config.operation.ruleelement.AbstractIterationOperation;
import org.jboss.windup.config.phase.Discovery;
import org.jboss.windup.config.phase.RulePhase;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.config.query.QueryGremlinCriterion;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.rules.apps.java.model.JavaMethodModel;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.Context;
import org.ocpsoft.rewrite.context.EvaluationContext;

import com.tinkerpop.blueprints.Predicate;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.gremlin.java.GremlinPipeline;

public class TestGremlinQueryOnlyRuleProvider extends WindupRuleProvider
{
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
                    pipeline.has(WindupVertexFrame.TYPE_PROP,new Predicate() {

                        @Override
                        public boolean evaluate(Object first, Object second)
                        {
                            List<String> firstString =(List<String>)first;
                            boolean match =firstString.contains(second);
                            return match;
                        }
                        
                    }, JavaMethodModel.TYPE);
                }
            }).as("javaMethods")
        )

        /*
         * If all conditions of the .when() clause were satisfied, the following conditions will be
         * evaluated
         */
        .perform(
            Iteration.over("javaMethods").as("javaMethod")
            .perform(new AbstractIterationOperation<JavaMethodModel>()
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
