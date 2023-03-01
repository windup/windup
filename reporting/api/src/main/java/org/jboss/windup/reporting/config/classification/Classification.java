package org.jboss.windup.reporting.config.classification;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;
import org.jboss.forge.furnace.util.Assert;
import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.metadata.RuleMetadataType;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.config.parameters.ParameterizedIterationOperation;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.FileReferenceModel;
import org.jboss.windup.graph.model.LinkModel;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.model.resource.SourceFileModel;
import org.jboss.windup.graph.service.LinkService;
import org.jboss.windup.graph.service.WindupConfigurationService;
import org.jboss.windup.reporting.config.Link;
import org.jboss.windup.reporting.config.TechnologiesIntersector;
import org.jboss.windup.reporting.model.IssueDisplayMode;
import org.jboss.windup.reporting.quickfix.Quickfix;
import org.jboss.windup.reporting.model.ClassificationModel;
import org.jboss.windup.reporting.service.ClassificationService;
import org.jboss.windup.reporting.service.TagSetService;
import org.jboss.windup.reporting.category.IssueCategory;
import org.jboss.windup.reporting.category.IssueCategoryModel;
import org.jboss.windup.reporting.category.IssueCategoryRegistry;
import org.jboss.windup.util.ExecutionStatistics;
import org.jboss.windup.util.Logging;
import org.ocpsoft.rewrite.config.Rule;
import org.ocpsoft.rewrite.context.Context;
import org.ocpsoft.rewrite.context.EvaluationContext;
import org.ocpsoft.rewrite.param.ParameterStore;
import org.ocpsoft.rewrite.param.RegexParameterizedPatternParser;

import static org.jboss.windup.reporting.config.TechnologiesIntersector.*;

/**
 * Classifies a {@link FileModel} {@link Iteration} payload.
 *
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 * @author <a href="mailto:dynawest@gmail.com">Ondrej Zizka</a>
 */
public class Classification extends ParameterizedIterationOperation<FileModel> implements ClassificationAs, ClassificationEffort,
        ClassificationDescription, ClassificationLink, ClassificationTags, ClassificationWithIssueCategory, ClassificationQuickfix,
        ClassificationWithIssueDisplayMode {
    private static final Logger LOG = Logging.get(Classification.class);

    private List<Link> links = new ArrayList<>();
    private Set<String> tags = new HashSet<>();
    private List<Quickfix> quickfixes = new ArrayList<>();
    private RegexParameterizedPatternParser classificationPattern;
    private RegexParameterizedPatternParser descriptionPattern;
    private int effort;
    private IssueCategory issueCategory = null;
    private IssueDisplayMode issueDisplayMode = IssueDisplayMode.Defaults.DEFAULT_DISPLAY_MODE;

    Classification(String variable) {
        super(variable);
    }

    Classification() {
        super();
    }

    /**
     * Create a new classification for the given ref.
     */
    public static ClassificationBuilderOf of(String variable) {
        return new ClassificationBuilderOf(variable);
    }

    /**
     * Classify the current {@link FileModel} as the given text.
     */
    public static ClassificationAs as(String classification) {
        Assert.notNull(classification, "Classification text must not be null.");
        Classification result = new Classification();
        result.classificationPattern = new RegexParameterizedPatternParser(classification);
        return result;
    }

    /**
     * @return the quickfixes
     */
    public List<Quickfix> getQuickfixes() {
        return quickfixes;
    }

    /**
     * Set the payload to the fileModel of the given instance even though the variable is not directly referencing it. This is mainly to simplify the
     * creation of the rule, when the FileModel itself is not being iterated but just a model referencing it.
     */
    @Override
    public FileModel resolvePayload(GraphRewrite event, EvaluationContext context, WindupVertexFrame payload) {
        checkVariableName(event, context);
        if (payload instanceof FileReferenceModel) {
            return ((FileReferenceModel) payload).getFile();
        }
        if (payload instanceof FileModel) {
            return (FileModel) payload;
        }
        return null;
    }

    /**
     * Gets the configured {@link IssueCategory} level.
     */
    public IssueCategory getIssueCategory() {
        return issueCategory;
    }

    /**
     * Sets the {@link IssueCategory} to a non-default level.
     */
    @Override
    public ClassificationWithIssueCategory withIssueCategory(IssueCategory issueCategory) {
        this.issueCategory = issueCategory;
        return this;
    }

    @Override
    public ClassificationWithIssueDisplayMode withIssueDisplayMode(IssueDisplayMode issueDisplayMode) {
        this.issueDisplayMode = issueDisplayMode;
        return this;
    }

    /**
     * Set the description of this {@link Classification}.
     */
    public ClassificationDescription withDescription(String description) {
        this.descriptionPattern = new RegexParameterizedPatternParser(description);
        return this;
    }

    /**
     * Add a {@link Link} to this {@link Classification}.
     */
    public ClassificationLink with(Link link) {
        this.links.add(link);
        return this;
    }

    public ClassificationQuickfix withQuickfix(Quickfix fix) {
        this.quickfixes.add(fix);
        return this;
    }

    /**
     * Add the given tags to this {@link Classification}.
     */
    public ClassificationTags withTags(Set<String> tags) {
        this.tags.addAll(tags);
        return this;
    }

    public Classification withEffort(int effort) {
        this.effort = effort;
        return this;
    }

    private Set<String> getTags() {
        return Collections.unmodifiableSet(tags);
    }

    @Override
    public void performParameterized(GraphRewrite event, EvaluationContext context, FileModel payload) {
        ExecutionStatistics.get().begin("Classification.performParameterized");
        try {
            /*
             * Check for duplicate classifications before we do anything. If a classification already exists, then we don't want to add another.
             */
            String description = null;

            if (descriptionPattern != null) {
                try {
                    description = descriptionPattern.getBuilder().build(event, context);
                } catch (Throwable t) {
                    LOG.log(Level.WARNING, "Failed to generate parameterized Classification description due to: " + t.getMessage(), t);
                    description = descriptionPattern.toString();
                }
            }

            String text;
            try {
                text = classificationPattern.getBuilder().build(event, context);
            } catch (Throwable t) {
                LOG.log(Level.WARNING, "Failed to generate parameterized Classification due to: " + t.getMessage(), t);
                text = classificationPattern.toString();
            }

            GraphContext graphContext = event.getGraphContext();
            ClassificationService classificationService = new ClassificationService(graphContext);

            ClassificationModel classification = classificationService.getUniqueByProperty(ClassificationModel.CLASSIFICATION, text);

            if (classification == null) {
                classification = classificationService.create();
                classification.setEffort(effort);
                classification.setIssueDisplayMode(this.issueDisplayMode);

                IssueCategoryModel issueCategoryModel;
                if (this.issueCategory == null)
                    issueCategoryModel = IssueCategoryRegistry.loadFromGraph(event.getGraphContext(), IssueCategoryRegistry.DEFAULT);
                else
                    issueCategoryModel = IssueCategoryRegistry.loadFromGraph(event.getGraphContext(), this.issueCategory.getCategoryID());

                classification.setIssueCategory(issueCategoryModel);
                classification.setDescription(StringUtils.trim(description));
                classification.setClassification(StringUtils.trim(text));

                Set<String> tags = new HashSet<>(this.getTags());
                TagSetService tagSetService = new TagSetService(event.getGraphContext());
                classification.setTagModel(tagSetService.getOrCreate(event, tags));

                classification.setRuleID(((Rule) context.get(Rule.class)).getId());

                LinkService linkService = new LinkService(graphContext);
                for (Link link : links) {
                    LinkModel linkModel = linkService.getOrCreate(
                            StringUtils.trim(link.getTitle()),
                            StringUtils.trim(link.getLink()));
                    classification.addLink(linkModel);
                }

                for (Quickfix quickfix : quickfixes) {
                    classification.addQuickfix(quickfix.createQuickfix(graphContext));
                }

                Rule rule = (Rule) context.get(Rule.class);
                Context ruleContext = rule instanceof Context ? (Context) rule : null;
                if (ruleContext != null) {
                    AbstractRuleProvider ruleProvider = (AbstractRuleProvider) ruleContext.get(RuleMetadataType.RULE_PROVIDER);

                    WindupConfigurationModel configuration = WindupConfigurationService.getConfigurationModel(event.getGraphContext());
                    classification.setTargetTechnologies(extractTargetTechnologies(configuration, ruleProvider.getMetadata()));
                    classification.setSourceTechnologies(extractSourceTechnologies(configuration, ruleProvider.getMetadata()));
                }
            }

            classificationService.attachClassification(event, classification, payload);
            if (payload instanceof SourceFileModel)
                ((SourceFileModel) payload).setGenerateSourceReport(true);
            LOG.fine("Classification added to " + payload.getPrettyPathWithinProject() + " [" + this + "] ");
        } finally {
            ExecutionStatistics.get().end("Classification.performParameterized");
        }
    }

    protected void setClassificationText(String classification) {
        this.classificationPattern = new RegexParameterizedPatternParser(classification);
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("Classification.as(").append(classificationPattern.getPattern()).append(")");
        if (descriptionPattern != null && !descriptionPattern.getPattern().trim().isEmpty())
            result.append(".withDescription(")
                    .append(StringUtils.abbreviate(("" + descriptionPattern).trim().replace("\n\n", "\n"), 50))
                    .append(")");
        if (effort != 0)
            result.append(".withEffort(").append(effort).append(")");
        if (links != null && !links.isEmpty())
            result.append(".with(").append(links).append(")");
        return result.toString();
    }

    public List<Link> getLinks() {
        return links;
    }

    public RegexParameterizedPatternParser getClassificationPattern() {
        return classificationPattern;
    }

    public RegexParameterizedPatternParser getDescriptionPattern() {
        return descriptionPattern;
    }

    public int getEffort() {
        return effort;
    }

    @Override
    public Set<String> getRequiredParameterNames() {
        Set<String> result = new HashSet<>(classificationPattern.getRequiredParameterNames());
        if (descriptionPattern != null)
            result.addAll(descriptionPattern.getRequiredParameterNames());
        return result;
    }

    @Override
    public void setParameterStore(ParameterStore store) {
        classificationPattern.setParameterStore(store);
        if (descriptionPattern != null)
            descriptionPattern.setParameterStore(store);
    }

}
