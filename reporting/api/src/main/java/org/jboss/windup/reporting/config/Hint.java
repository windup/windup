package org.jboss.windup.reporting.config;

import org.apache.commons.lang3.StringUtils;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.jboss.forge.furnace.util.Assert;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.parameters.ParameterizedIterationOperation;
import org.jboss.windup.graph.model.FileLocationModel;
import org.jboss.windup.graph.model.FileReferenceModel;
import org.jboss.windup.graph.model.LinkModel;
import org.jboss.windup.graph.model.WindupFrame;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.model.resource.SourceFileModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.reporting.category.IssueCategory;
import org.jboss.windup.reporting.category.IssueCategoryModel;
import org.jboss.windup.reporting.category.IssueCategoryRegistry;
import org.jboss.windup.reporting.model.EffortReportModel;
import org.jboss.windup.reporting.model.InlineHintModel;
import org.jboss.windup.reporting.model.IssueDisplayMode;
import org.jboss.windup.reporting.model.QuickfixModel;
import org.jboss.windup.reporting.quickfix.Quickfix;
import org.jboss.windup.reporting.service.TagSetService;
import org.jboss.windup.util.ExecutionStatistics;
import org.jboss.windup.util.Logging;
import org.ocpsoft.rewrite.config.OperationBuilder;
import org.ocpsoft.rewrite.config.Rule;
import org.ocpsoft.rewrite.context.EvaluationContext;
import org.ocpsoft.rewrite.param.ParameterStore;
import org.ocpsoft.rewrite.param.RegexParameterizedPatternParser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Used as an intermediate to support the addition of {@link InlineHintModel} objects to the graph via an Operation.
 */
public class Hint extends ParameterizedIterationOperation<FileLocationModel> implements HintText, HintLink, HintWithIssueCategory, HintEffort, HintQuickfix, HintDisplayMode
{
    private static final Logger LOG = Logging.get(Hint.class);
    private static final String HINT_PIPELINE_LABEL = "InlineHintModels";

    private RegexParameterizedPatternParser hintTitlePattern;
    private RegexParameterizedPatternParser hintTextPattern;
    private int effort;
    private IssueCategory issueCategory;
    private List<Link> links = new ArrayList<>();
    private Set<String> tags = Collections.emptySet();
    private List<Quickfix> quickfixes = new ArrayList<>();
    private IssueDisplayMode issueDisplayMode = IssueDisplayMode.Defaults.DEFAULT_DISPLAY_MODE;

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
    public HintWithIssueCategory withIssueCategory(IssueCategory issueCategory)
    {
        this.issueCategory = issueCategory;
        return this;
    }

    @Override
    public HintText withDisplayMode(IssueDisplayMode displayMode)
    {
        this.issueDisplayMode = displayMode;
        return this;
    }

    /**
     * Returns the currently set {@link IssueCategory}.
     */
    public IssueCategory getIssueCategory()
    {
        return this.issueCategory;
    }

    @Override
    public void performParameterized(final GraphRewrite event, final EvaluationContext context, final FileLocationModel locationModel)
    {
        ExecutionStatistics.get().begin("Hint.performParameterized");
        try
        {
            GraphService<InlineHintModel> service = new GraphService<>(event.getGraphContext(), InlineHintModel.class);

            String hintText;
            try
            {
                hintText = hintTextPattern.getBuilder().build(event, context);
            }
            catch (Throwable t)
            {
                LOG.log(Level.WARNING, "Failed to generate parameterized Hint body due to: " + t.getMessage(), t);
                hintText = hintTextPattern.toString();
            }
            hintText = StringUtils.trim(hintText);
            
            String hintTitle;
            if (hintTitlePattern != null)
            {
                try
                {
                    hintTitle = StringUtils.trim(hintTitlePattern.getBuilder().build(event, context));
                }
                catch (Throwable t)
                {
                    LOG.log(Level.WARNING, "Failed to generate parameterized Hint title due to: " + t.getMessage(), t);
                    hintTitle= hintTitlePattern.toString().trim();
                }
            }
            else
            {
                // If there is no title, just use the description of the location
                // (eg, 'Constructing com.otherproduct.Foo()')
                hintTitle = locationModel.getDescription();
            }
            
            String ruleId = ((Rule) context.get(Rule.class)).getId();

            // NOT checking links, quickfixes and tags since they are not parameterized hence checking the same rule generated the hint 
            // means the links and quickfixes will be mandatory the same as well since they can not change based on parameters
            boolean hintAlreadyAdded = new GraphTraversalSource(event.getGraphContext().getGraph())
                    .V().match(
                            __.as(HINT_PIPELINE_LABEL)
                                    .has(WindupFrame.TYPE_PROP, P.eq(InlineHintModel.TYPE))
                                    .has(InlineHintModel.RULE_ID, ruleId)
                                    .has(FileLocationModel.LINE_NUMBER, locationModel.getLineNumber())
                                    .has(FileLocationModel.COLUMN_NUMBER, locationModel.getColumnNumber())
                                    .has(FileLocationModel.LENGTH, locationModel.getLength())
                                    .has(EffortReportModel.EFFORT, effort)
                                    .has(InlineHintModel.ISSUE_DISPLAY_MODE, issueDisplayMode.name())
                                    .has(InlineHintModel.HINT, hintText)
                                    .has(InlineHintModel.TITLE, hintTitle),
                            __.as(HINT_PIPELINE_LABEL).out(FileReferenceModel.FILE_MODEL).has(FileModel.FILE_PATH, locationModel.getFile().getFilePath()),
                            __.as(HINT_PIPELINE_LABEL).out(InlineHintModel.FILE_LOCATION_REFERENCE).has(FileLocationModel.SOURCE_SNIPPIT, locationModel.getSourceSnippit())
                    ).select(HINT_PIPELINE_LABEL).hasNext();
            if (hintAlreadyAdded)
            {
                if (LOG.isLoggable(Level.WARNING))
                    LOG.warning(String.format("Not an error but the hint '%s' has been already added from the rule '%s' to file %s in the same line (%d) and column (%d) so it will NOT be added again.", 
                        hintTitle, ruleId, locationModel.getFile().getFilePath(), locationModel.getLineNumber(), locationModel.getColumnNumber()));
                return;
            }

            InlineHintModel hintModel = service.create();
            hintModel.setRuleID(ruleId);
            hintModel.setLineNumber(locationModel.getLineNumber());
            hintModel.setColumnNumber(locationModel.getColumnNumber());
            hintModel.setLength(locationModel.getLength());
            hintModel.setFileLocationReference(locationModel);
            hintModel.setFile(locationModel.getFile());
            hintModel.setEffort(effort);
            hintModel.setIssueDisplayMode(this.issueDisplayMode);

            IssueCategoryRegistry issueCategoryRegistry = IssueCategoryRegistry.instance(event.getRewriteContext());
            IssueCategoryModel issueCategoryModel;
            if (this.issueCategory == null)
                issueCategoryModel = issueCategoryRegistry.loadFromGraph(event.getGraphContext(), IssueCategoryRegistry.DEFAULT);
            else
                issueCategoryModel = issueCategoryRegistry.loadFromGraph(event.getGraphContext(), this.issueCategory.getCategoryID());

            hintModel.setIssueCategory(issueCategoryModel);
            hintModel.setTitle(hintTitle);
            hintModel.setHint(hintText);

            GraphService<LinkModel> linkService = new GraphService<>(event.getGraphContext(), LinkModel.class);
            for (Link link : links)
            {
                LinkModel linkModel = linkService.create();
                linkModel.setDescription(StringUtils.trim(link.getTitle()));
                linkModel.setLink(StringUtils.trim(link.getLink()));
                hintModel.addLink(linkModel);
            }

            GraphService<QuickfixModel> quickfixService = new GraphService<>(event.getGraphContext(), QuickfixModel.class);
            for (Quickfix quickfix : quickfixes)
            {
                hintModel.addQuickfix(quickfix.createQuickfix(event.getGraphContext()));
            }

            Set<String> tags = new HashSet<>(this.getTags());
            TagSetService tagSetService = new TagSetService(event.getGraphContext());
            hintModel.setTagModel(tagSetService.getOrCreate(event, tags));

            if (locationModel.getFile() instanceof SourceFileModel)
                ((SourceFileModel) locationModel.getFile()).setGenerateSourceReport(true);

            LOG.fine("Hint added to " + locationModel.getFile().getPrettyPathWithinProject()
                    + ":\n    " + this.toString(hintModel.getTitle(), null));
        }
        finally
        {
            ExecutionStatistics.get().end("Hint.performParameterized");
        }
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
        final Set<String> result = new LinkedHashSet<>();
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
        if (text != null)
            result.append(".withText(\"").append(text).append("\")");
        if (effort != 0)
            result.append(".withEffort(").append(effort).append(")");
        if (links != null && !links.isEmpty())
            result.append(".with(").append(links).append(")");
        if (tags != null && !tags.isEmpty())
            result.append(".withTags(").append(tags).append(")");
        return result.toString();
    }

    /**
     * @return the quickfixes
     */
    public List<Quickfix> getQuickfixes()
    {
        return quickfixes;
    }

    /**
     * @param quickfix the quickfixes to set
     */
    public HintQuickfix withQuickfix(Quickfix quickfix)
    {
        this.quickfixes.add(quickfix);
        return this;
    }
}
