package org.jboss.windup.reporting.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import org.apache.commons.collections.set.UnmodifiableSet;

import org.jboss.forge.furnace.util.Assert;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.parameters.ParameterizedIterationOperation;
import org.jboss.windup.graph.model.LinkModel;
import org.jboss.windup.graph.model.resource.SourceFileModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.reporting.model.InlineHintModel;
import org.jboss.windup.reporting.model.Severity;
import org.jboss.windup.rules.files.model.FileLocationModel;
import org.ocpsoft.rewrite.config.OperationBuilder;
import org.ocpsoft.rewrite.config.Rule;
import org.ocpsoft.rewrite.context.EvaluationContext;
import org.ocpsoft.rewrite.param.ParameterStore;
import org.ocpsoft.rewrite.param.RegexParameterizedPatternParser;

/**
 * Used as an intermediate to support the addition of {@link InlineHintModel} objects to the graph via an Operation.
 */
public class Hint extends ParameterizedIterationOperation<FileLocationModel> implements HintText, HintLink, HintSeverity, HintEffort
{
    private static final Logger log = Logger.getLogger(Hint.class.getName());

    public static final Severity DEFAULT_SEVERITY = Severity.OPTIONAL;

    private RegexParameterizedPatternParser hintTitlePattern;
    private RegexParameterizedPatternParser hintTextPattern;
    private int effort;
    private Severity severity = DEFAULT_SEVERITY;
    private List<Link> links = new ArrayList<>();
    private Set<String> tags = Collections.EMPTY_SET;

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
     * Create a new {@link Hint} with the specified title.
     */
    public static HintBuilderTitle titled(String title)
    {
        return new HintBuilderTitle(title);
    }

    @Override
    public HintSeverity withSeverity(Severity severity)
    {
        this.severity = severity;
        return this;
    }

    /**
     * Returns the currently set {@link Severity}.
     */
    public Severity getSeverity()
    {
        return severity;
    }

    /**
     * Create a new {@link Hint} in the current {@link FileLocationModel}, and specify the text or content to be
     * displayed in the report.
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
        hintModel.setRuleID(((Rule) context.get(Rule.class)).getId());
        hintModel.setLineNumber(locationModel.getLineNumber());
        hintModel.setColumnNumber(locationModel.getColumnNumber());
        hintModel.setLength(locationModel.getLength());
        hintModel.setFileLocationReference(locationModel);
        hintModel.setFile(locationModel.getFile());
        hintModel.setEffort(effort);
        hintModel.setSeverity(this.severity);
        if (hintTitlePattern != null)
        {
            hintModel.setTitle(hintTitlePattern.getBuilder().build(event, context));
        }
        else
        {
            /*
             * if there is no title, just use the description of the location (eg, 'Constructing
             * com.otherproduct.Foo()')
             */
            hintModel.setTitle(locationModel.getDescription());
        }

        String hintText = hintTextPattern.getBuilder().build(event, context);
        hintModel.setHint(hintText);

        GraphService<LinkModel> linkService = new GraphService<>(event.getGraphContext(), LinkModel.class);
        for (Link link : links)
        {
            LinkModel linkModel = linkService.create();
            linkModel.setDescription(link.getTitle());
            linkModel.setLink(link.getLink());
            hintModel.addLink(linkModel);
        }

        hintModel.setTags(this.getTags());

        if (locationModel.getFile() instanceof SourceFileModel)
            ((SourceFileModel) locationModel.getFile()).setGenerateSourceReport(true);

        log.info("Hint added to " + locationModel.getFile().getPrettyPathWithinProject() + " [" + this.toString(hintModel.getTitle(), hintText)
                    + "] ");
    }

    @Override
    public HintEffort withEffort(int effort)
    {
        this.effort = effort;
        return this;
    }

    @Override
    public OperationBuilder withTags(Set<String> tags)
    {
        this.tags = tags;
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

    public Set<String> getTags()
    {
        return Collections.unmodifiableSet(tags);
    }

    @Override
    public Set<String> getRequiredParameterNames()
    {
        final Set<String> result = new LinkedHashSet<String>();
        result.addAll(hintTextPattern.getRequiredParameterNames());
        if (hintTitlePattern != null)
            result.addAll(hintTitlePattern.getRequiredParameterNames());
        return result;
    }

    @Override
    public void setParameterStore(ParameterStore store)
    {
        hintTextPattern.setParameterStore(store);
        if (hintTitlePattern != null)
            hintTitlePattern.setParameterStore(store);
    }

    @Override
    public String toString()
    {
        String title = "";
        if (hintTitlePattern != null)
            title = hintTitlePattern.getPattern();
        return toString(title, hintTextPattern.getPattern());
    }

    private String toString(String title, String text)
    {
        StringBuilder result = new StringBuilder();
        result.append("Hint");
        if (title != null)
            result.append(".titled(\"").append(title).append("\")");
        result.append(".withText(\"" + text + "\")");
        if (effort != 0)
            result.append(".withEffort(" + effort + ")");
        if (links != null && !links.isEmpty())
            result.append(".with(" + links + ")");
        if (tags != null && !tags.isEmpty())
            result.append(".withTags(" + tags + ")");
        return result.toString();
    }

}
