package org.jboss.windup.reporting.config;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.operation.ruleelement.AbstractIterationOperation;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.reporting.model.FileLocationModel;
import org.jboss.windup.reporting.model.InlineHintModel;
import org.ocpsoft.rewrite.config.OperationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

public class Hint extends AbstractIterationOperation<FileModel>
{
    private String hintText;
    private int effort;
    private String locationVar;

    private Hint(String variable)
    {
        super(FileModel.class, variable);
    }

    @Override
    public void perform(GraphRewrite event, EvaluationContext context, FileModel payload)
    {
        GraphService<InlineHintModel> service = new GraphService<>(event.getGraphContext(), InlineHintModel.class);

        InlineHintModel hintModel = service.create();
        FileLocationModel locationModel = (FileLocationModel) resolveVariable(event, locationVar);

        hintModel.setLineNumber(locationModel.getLineNumber());
        hintModel.setColumnNumber(locationModel.getColumnNumber());
        hintModel.setLength(locationModel.getLength());

        hintModel.setEffort(effort);
        hintModel.setHint(hintText);
    }

    /**
     * Create a new {@link Hint} in the {@link FileModel} resolved by the given variable.
     */
    public static Hint in(String fileVariable)
    {
        return new Hint(fileVariable);
    }

    /**
     * Display the {@link Hint} at the location resolved by the given variable.
     */
    public Hint at(String locationVariable)
    {
        this.locationVar = locationVariable;
        return this;
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
