package org.jboss.windup.config.graph;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.Variables;
import org.jboss.windup.config.operation.GraphOperation;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.config.operation.ruleelement.AbstractIterationOperation;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.service.GraphService;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * Adds the given type to a {@link WindupVertexFrame} in the graph.
 * 
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 *
 */
public class TypeOperation extends AbstractIterationOperation<WindupVertexFrame>
{
    private Class<? extends WindupVertexFrame> newType;

    private TypeOperation(String variableName, Class<? extends WindupVertexFrame> newType)
    {
        super(WindupVertexFrame.class, variableName);
        this.newType = newType;
    }

    public static GraphOperation addType(String variableName, Class<? extends WindupVertexFrame> newType)
    {
        return new TypeOperation(variableName, newType);
    }

    @Override
    public void perform(GraphRewrite event, EvaluationContext context, WindupVertexFrame payload)
    {
        Variables varStack = Variables.instance(event);
        WindupVertexFrame newFrame = GraphService.addTypeToModel(event.getGraphContext(), payload, newType);
        Iteration.setCurrentPayload(varStack, getVariableName(), newFrame);
    }
}
