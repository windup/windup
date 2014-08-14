package org.jboss.windup.reporting.config;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.operation.ruleelement.AbstractIterationOperation;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.reporting.model.FileLocationModel;
import org.jboss.windup.reporting.model.InlineHintModel;
import org.ocpsoft.rewrite.config.OperationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

public class Hint extends AbstractIterationOperation<FileLocationModel>
{
    private String hintText;
    private int effort;

    private Hint(String variable)
    {
        super(variable);
    }
    
    private Hint()
    {
        super();
    }

    @Override
    public void perform(GraphRewrite event, EvaluationContext context, FileLocationModel locationModel)
    {
        GraphService<InlineHintModel> service = new GraphService<>(event.getGraphContext(), InlineHintModel.class);

        InlineHintModel hintModel = service.create();

        hintModel.setLineNumber(locationModel.getLineNumber());
        hintModel.setColumnNumber(locationModel.getColumnNumber());
        hintModel.setLength(locationModel.getLength());

        hintModel.setFile(locationModel.getFile());

        hintModel.setEffort(effort);
        hintModel.setHint(hintText);
    }

    /**
     * Create a new {@link Hint} in the {@link FileLocationModel} resolved by the given variable.
     */
    public static Hint in(String fileVariable)
    {
        return new Hint(fileVariable);
    }
    
    public static Hint havingText(String text)
    {
        Hint hint = new Hint();
        hint.hintText = text;
        return hint;
    }

    public Hint withText(String text)
    {
        this.hintText = text;
        return this;
    }

    public OperationBuilder withEffort(int effort)
    {
        this.effort = effort;
        return this;
    }

}
