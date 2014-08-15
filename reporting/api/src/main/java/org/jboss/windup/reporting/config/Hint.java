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

    protected Hint(String variable)
    {
        super(variable);
    }

    protected Hint()
    {
        super();
    }

    /**
     * Create a new {@link Hint} in the {@link FileLocationModel} resolved by the given variable.
     */
    public static HintBuilderIn in(String fileVariable)
    {
        return new HintBuilderIn(fileVariable);
    }

    /**
     * Create a new {@link Hint} in the current {@link FileLocationModel}, and specify the text or content to be
     * displayed in the report.
     */
    public static Hint withText(String text)
    {
        Hint hint = new Hint();
        hint.hintText = text;
        return hint;
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
     * Specify the effort or content to be displayed in the report.
     */
    public OperationBuilder withEffort(int effort)
    {
        this.effort = effort;
        return this;
    }

    /**
     * Set the inner hint text on this instance.
     */
    protected void setText(String text)
    {
        this.hintText = text;
    }

    protected String getText()
    {
        return this.hintText;
    }

    protected int getEffort()
    {
        return effort;
    }
}
