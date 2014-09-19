package org.jboss.windup.reporting.config;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.jboss.forge.furnace.util.Assert;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.operation.ruleelement.AbstractIterationOperation;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.reporting.model.FileLocationModel;
import org.jboss.windup.reporting.model.InlineHintModel;
import org.jboss.windup.reporting.model.LinkModel;
import org.ocpsoft.rewrite.config.OperationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * Used as an intermediate to support the addition of {@link InlineHintModel} objects to the graph via an Operation.
 */
public class Hint extends AbstractIterationOperation<FileLocationModel>
{
    private static final Logger log = Logger.getLogger(Hint.class.getName());

    private String hintText;
    private int effort;
    private List<Link> links = new ArrayList<>();

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
        Assert.notNull(text, "Hint text must not be null.");
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
        hintModel.setFileLocationReference(locationModel);
        hintModel.setFile(locationModel.getFile());
        hintModel.setEffort(effort);
        hintModel.setHint(hintText);

        GraphService<LinkModel> linkService = new GraphService<>(event.getGraphContext(), LinkModel.class);
        for (Link link : links)
        {
            LinkModel linkModel = linkService.create();
            linkModel.setDescription(link.getDescription());
            linkModel.setLink(link.getLink());
            hintModel.addLink(linkModel);
        }

        log.info("Hint added to " + locationModel.getFile().getPrettyPathWithinProject() + " [" + this + "] ");
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
     * Add a {@link Link} to this {@link Hint}.
     */
    public Hint with(Link link)
    {
        this.links.add(link);
        return this;
    }

    /**
     * Set the inner hint text on this instance.
     */
    protected void setText(String text)
    {
        this.hintText = text;
    }

    @Override
    public String toString()
    {
        StringBuilder result = new StringBuilder();
        result.append("Hint.withText(\"" + hintText + "\")");
        if (effort != 0)
            result.append(".withEffort(" + effort + ")");
        if (links != null && !links.isEmpty())
            result.append(".with(" + links + ")");
        return result.toString();
    }

}
