package org.jboss.windup.reporting.config;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.operation.ruleelement.AbstractIterationOperation;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.reporting.model.FileReferenceModel;
import org.ocpsoft.rewrite.context.EvaluationContext;

public class WhiteList extends AbstractIterationOperation<FileModel>
{
    private WhiteList(String variable)
    {
        super(variable);
    }
    
    private WhiteList()
    {
        super();
    }
    
    public static WhiteList add() {
        return new WhiteList();
    }
    
    /**
     * Set the payload to the fileModel of the given instance even though the variable is not directly referencing it.
     * This is mainly to simplify the creation of the rule, when the FileModel itself is not being iterated but just a model
     * referencing it.
     * 
     */
    @Override
    public void perform(GraphRewrite event, EvaluationContext context)
    {
        WindupVertexFrame payload = resolveVariable(event, getVariableName());
        if(payload instanceof FileReferenceModel) {
            perform(event, context,((FileReferenceModel)payload).getFile());
        } else {
            perform(event, context);
        }
        
    }


    @Override
    public void perform(GraphRewrite event, EvaluationContext context, FileModel fileModel)
    {
        fileModel.setWhiteList(true);
    }

}
