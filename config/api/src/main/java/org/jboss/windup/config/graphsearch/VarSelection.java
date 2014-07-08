package org.jboss.windup.config.graphsearch;

import java.util.Collections;
import java.util.List;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.condition.GraphCondition;
import org.jboss.windup.config.runner.VarStack;
import org.jboss.windup.graph.VertexFrameAsVertexIterable;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.ocpsoft.rewrite.context.EvaluationContext;

import com.tinkerpop.blueprints.Vertex;

public class VarSelection extends GraphCondition
{
    private final String inputVar;
    private final GraphSearchConditionBuilderGremlin gremlin;

    public VarSelection(String inputVar, String outputVar)
    {
        this.inputVar = inputVar;
        List<Vertex> initialList = Collections.emptyList();
        gremlin = new GraphSearchConditionBuilderGremlin(outputVar, initialList);
    }

    public static VarSelection query(String inputVar, String outputVar)
    {
        return new VarSelection(inputVar, outputVar);
    }

    public VarSelection addCriterion(GremlinPipelineCriterion criterion)
    {
        gremlin.addCriterion(criterion);
        return this;
    }

    @Override
    public boolean evaluate(GraphRewrite event, EvaluationContext context)
    {
        Iterable<WindupVertexFrame> inputFrames = VarStack.instance(event).findVariable(inputVar);
        VertexFrameAsVertexIterable iterable = new VertexFrameAsVertexIterable(inputFrames);
        gremlin.setInitialVertices(iterable);
        return gremlin.evaluate(event, context);
    }
}
