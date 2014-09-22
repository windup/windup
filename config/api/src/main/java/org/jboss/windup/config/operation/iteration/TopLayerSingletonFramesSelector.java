package org.jboss.windup.config.operation.iteration;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.Variables;
import org.jboss.windup.config.selectors.FramesSelector;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.ocpsoft.rewrite.context.EvaluationContext;

public class TopLayerSingletonFramesSelector implements FramesSelector
{

    private String varName;
    
    @Override
    public Iterable<WindupVertexFrame> getFrames(GraphRewrite event, EvaluationContext context)
    {
        Variables instance = Variables.instance(event);
        this.varName=instance.getTopLevelSingletonName();
        return instance.findVariable(varName);
    }
}
