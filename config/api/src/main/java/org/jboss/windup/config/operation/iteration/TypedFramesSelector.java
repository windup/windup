package org.jboss.windup.config.operation.iteration;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.Variables;
import org.jboss.windup.config.selectors.FramesSelector;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * Selection of frames having all variables of the given type (regardless of the variable name).
 * 
 * @author mbriskar
 *
 */
public class TypedFramesSelector implements FramesSelector
{
    private Class<? extends WindupVertexFrame> framesModel;

    public TypedFramesSelector(Class<? extends WindupVertexFrame> framesModel)
    {
        this.framesModel = framesModel;
    }

    @Override
    public Iterable<? extends WindupVertexFrame> getFrames(GraphRewrite event, EvaluationContext context)
    {
        final Iterable<? extends WindupVertexFrame> frames = Variables.instance(event).findVariableOfType(framesModel);
        return frames;
    }
}