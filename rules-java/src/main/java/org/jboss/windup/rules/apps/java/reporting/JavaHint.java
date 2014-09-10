package org.jboss.windup.rules.apps.java.reporting;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.operation.ruleelement.AbstractIterationOperation;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.reporting.model.FileLocationModel;
import org.jboss.windup.rules.apps.java.scan.ast.JavaInlineHintModel;
import org.jboss.windup.rules.apps.java.scan.ast.TypeReferenceModel;
import org.ocpsoft.rewrite.config.OperationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * Used as an intermediate to support the addition of {@link JavaInlineHintModel} objects to the graph via an Operation.
 * 
 * @author jsightler <jesse.sightler@gmail.com>
 */
public class JavaHint extends AbstractIterationOperation<FileLocationModel>
{
    private String hintText;
    private int effort;

    public JavaHint(String varName)
    {
        super(varName);
    }

    public JavaHint()
    {
        super();
    }

    /**
     * Create a new {@link JavaHint} in the {@link FileModel} resolved by the given variable.
     */
    public static JavaHintBuilderIn in(String fileVariable)
    {
        return new JavaHintBuilderIn(fileVariable);
    }

    /**
     * Create a new {@link JavaHint} in the current {@link FileLocationModel}, and specify the text or content to be
     * displayed in the report.
     */
    public static JavaHint withText(String text)
    {
        JavaHint javaHint = new JavaHint();
        javaHint.hintText = text;
        return javaHint;
    }

    @Override
    public void perform(GraphRewrite event, EvaluationContext context, FileLocationModel payload)
    {
        GraphService<JavaInlineHintModel> service = new GraphService<>(event.getGraphContext(),
                    JavaInlineHintModel.class);

        JavaInlineHintModel hintModel = service.create();

        if (payload instanceof TypeReferenceModel)
        {
            TypeReferenceModel typeReferenceModel = (TypeReferenceModel) payload;
            hintModel.setTypeReferenceModel(typeReferenceModel);
        }

        hintModel.setLineNumber(payload.getLineNumber());
        hintModel.setColumnNumber(payload.getColumnNumber());
        hintModel.setLength(payload.getLength());

        hintModel.setFile(payload.getFile());

        hintModel.setEffort(this.effort);
        hintModel.setHint(this.hintText);
    }

    /**
     * Specify the effort or content to be displayed in the report.
     */
    public OperationBuilder withEffort(int effort)
    {
        this.effort = effort;
        return this;
    }

    void setText(String hintText)
    {
        this.hintText = hintText;
    }
}
