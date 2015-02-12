package org.jboss.windup.reporting.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.jboss.forge.furnace.util.Assert;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.parameters.ParameterizedIterationOperation;
import org.jboss.windup.graph.model.resource.SourceFileModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.reporting.model.InlineHintModel;
import org.jboss.windup.reporting.model.LinkModel;
import org.jboss.windup.rules.files.model.FileLocationModel;
import org.ocpsoft.rewrite.config.OperationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;
import org.ocpsoft.rewrite.param.ParameterStore;
import org.ocpsoft.rewrite.param.RegexParameterizedPatternParser;

/**
 * Used as an intermediate to support the addition of {@link InlineHintModel} objects to the graph via an Operation.
 */
public class Hint extends ParameterizedIterationOperation<FileLocationModel> implements HintText, HintLink
{
    private static final Logger log = Logger.getLogger(Hint.class.getName());

    private RegexParameterizedPatternParser hintTitlePattern;
    private RegexParameterizedPatternParser hintTextPattern;
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

    public static HintBuilderTitle titled(String title)
    {
        return new HintBuilderTitle(title);
    }

    /**
     * Create a new {@link Hint} in the current {@link FileLocationModel}, and specify the text or content to be displayed in the report.
     */
    public static HintText withText(String text)
    {
        Assert.notNull(text, "Hint text must not be null.");
        Hint hint = new Hint();
        hint.setText(text);
        return hint;
    }

    @Override
    public void performParameterized(GraphRewrite event, EvaluationContext context, FileLocationModel locationModel)
    {
        GraphService<InlineHintModel> service = new GraphService<>(event.getGraphContext(), InlineHintModel.class);

        InlineHintModel hintModel = service.create();
        hintModel.setLineNumber(locationModel.getLineNumber());
        hintModel.setColumnNumber(locationModel.getColumnNumber());
        hintModel.setLength(locationModel.getLength());
        hintModel.setFileLocationReference(locationModel);
        hintModel.setFile(locationModel.getFile());
        hintModel.setEffort(effort);
        if (hintTitlePattern != null)
        {
            hintModel.setTitle(hintTitlePattern.getBuilder().build(event, context));
        }
        else
        {
            // if there is no title, just use the description of the location (eg, 'Constructing com.otherproduct.Foo()')
            hintModel.setTitle(locationModel.getDescription());
        }
        hintModel.setHint(hintTextPattern.getBuilder().build(event, context));

        GraphService<LinkModel> linkService = new GraphService<>(event.getGraphContext(), LinkModel.class);
        for (Link link : links)
        {
            LinkModel linkModel = linkService.create();
            linkModel.setDescription(link.getDescription());
            linkModel.setLink(link.getLink());
            hintModel.addLink(linkModel);
        }

        if (locationModel.getFile() instanceof SourceFileModel)
            ((SourceFileModel) locationModel.getFile()).setGenerateSourceReport(true);
        log.info("Hint added to " + locationModel.getFile().getPrettyPathWithinProject() + " [" + this + "] ");
    }

    @Override
    public OperationBuilder withEffort(int effort)
    {
        this.effort = effort;
        return this;
    }

    @Override
    public HintLink with(Link link)
    {
        this.links.add(link);
        return this;
    }

    /**
     * Set the inner hint text on this instance.
     */
    protected void setText(String text)
    {
        this.hintTextPattern = new RegexParameterizedPatternParser(text);
    }

    protected void setTitle(String title)
    {
        this.hintTitlePattern = new RegexParameterizedPatternParser(title);
    }

    @Override
    public String toString()
    {
        StringBuilder result = new StringBuilder();
        result.append("Hint");
        if (hintTitlePattern != null)
            result.append(".titled(\"").append(hintTitlePattern.getPattern()).append("\")");
        result.append(".withText(\"" + hintTextPattern.getPattern() + "\")");
        if (effort != 0)
            result.append(".withEffort(" + effort + ")");
        if (links != null && !links.isEmpty())
            result.append(".with(" + links + ")");
        return result.toString();
    }

    public RegexParameterizedPatternParser getHintText()
    {
        return hintTextPattern;
    }

    public int getEffort()
    {
        return effort;
    }

    public List<Link> getLinks()
    {
        return links;
    }

    @Override
    public Set<String> getRequiredParameterNames()
    {
        return hintTextPattern.getRequiredParameterNames();
    }

    @Override
    public void setParameterStore(ParameterStore store)
    {
        hintTextPattern.setParameterStore(store);
    }

}
