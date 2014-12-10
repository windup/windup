package org.jboss.windup.config.operation.iteration;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.Variables;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.config.selectors.FramesSelector;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.ocpsoft.rewrite.context.EvaluationContext;

public class TopLayerSingletonFramesSelector implements FramesSelector
{

    private String varName;

    @Override
    public Iterable<? extends WindupVertexFrame> getFrames(GraphRewrite event, EvaluationContext context)
    {
        Variables variables = Variables.instance(event);
        this.varName = Iteration.getPayloadVariableName(event, context);
        return variables.findVariable(varName);
    }
}
