package org.jboss.windup.reporting.config.classification;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.jboss.forge.furnace.util.Assert;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.config.parameters.ParameterizedIterationOperation;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.LinkModel;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.model.resource.SourceFileModel;
import org.jboss.windup.graph.service.LinkService;
import org.jboss.windup.reporting.config.Link;
import org.jboss.windup.reporting.model.ClassificationModel;
import org.jboss.windup.reporting.model.Severity;
import org.jboss.windup.reporting.service.ClassificationService;
import org.jboss.windup.rules.files.model.FileReferenceModel;
import org.ocpsoft.rewrite.config.Rule;
import org.ocpsoft.rewrite.context.EvaluationContext;
import org.ocpsoft.rewrite.param.ParameterStore;
import org.ocpsoft.rewrite.param.RegexParameterizedPatternParser;

/**
 * Classifies a {@link FileModel} {@link Iteration} payload.
 *
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 * @author <a href="mailto:dynawest@gmail.com">Ondrej Zizka</a>
 */
public class Classification extends ParameterizedIterationOperation<FileModel> implements ClassificationAs, ClassificationEffort,
            ClassificationDescription, ClassificationLink, ClassificationTags, ClassificationSeverity
{
    public static final Severity DEFAULT_SEVERITY = Severity.OPTIONAL;
    private static final Logger log = Logger.getLogger(Classification.class.getName());

    private List<Link> links = new ArrayList<>();
    private Set<String> tags = new HashSet<>();

    private RegexParameterizedPatternParser classificationPattern;
    private RegexParameterizedPatternParser descriptionPattern;
    private int effort;
    private Severity severity = DEFAULT_SEVERITY;

    Classification(String variable)
    {
        super(variable);
    }

    Classification()
    {
        super();
    }

    /**
     * Set the payload to the fileModel of the given instance even though the variable is not directly referencing it. This is mainly to simplify the
     * creation of the rule, when the FileModel itself is not being iterated but just a model referencing it.
     *
     */
    @Override
    public FileModel resolvePayload(GraphRewrite event, EvaluationContext context, WindupVertexFrame payload)
    {
        checkVariableName(event, context);
        if (payload instanceof FileReferenceModel)
        {
            return ((FileReferenceModel) payload).getFile();
        }
        if (payload instanceof FileModel)
        {
            return (FileModel) payload;
        }
        return null;
    }

    /**
     * Create a new classification for the given ref.
     */
    public static ClassificationBuilderOf of(String variable)
    {
        return new ClassificationBuilderOf(variable);
    }

    /**
     * Gets the configured {@link Severity} level.
     */
    public Severity getSeverity()
    {
        return severity;
    }

    /**
     * Sets the {@link Severity} to a non-default level.
     */
    @Override
    public ClassificationSeverity withSeverity(Severity severity)
    {
        this.severity = severity;
        return this;
    }

    /**
     * Set the description of this {@link Classification}.
     */
    public ClassificationDescription withDescription(String description)
    {
        this.descriptionPattern = new RegexParameterizedPatternParser(description);
        return this;
    }

    /**
     * Add a {@link Link} to this {@link Classification}.
     */
    public ClassificationLink with(Link link)
    {
        this.links.add(link);
        return this;
    }

    /**
     * Add the given tags to this {@link Classification}.
     */
    public ClassificationTags withTags(Set<String> tags)
    {
        this.tags.addAll(tags);
        return this;
    }

    public Classification withEffort(int effort)
    {
        this.effort = effort;
        return this;
    }

    /**
     * Classify the current {@link FileModel} as the given text.
     */
    public static ClassificationAs as(String classification)
    {
        Assert.notNull(classification, "Classification text must not be null.");
        Classification result = new Classification();
        result.classificationPattern = new RegexParameterizedPatternParser(classification);
        return result;
    }

    @Override
    public void performParameterized(GraphRewrite event, EvaluationContext context, FileModel payload)
    {
        /*
         * Check for duplicate classifications before we do anything. If a classification already exists, then we don't want to add another.
         */
        String description = null;
        if (descriptionPattern != null)
            description = descriptionPattern.getBuilder().build(event, context);
        String text = classificationPattern.getBuilder().build(event, context);

        GraphContext graphContext = event.getGraphContext();
        ClassificationService classificationService = new ClassificationService(graphContext);

        ClassificationModel classification = classificationService.getUniqueByProperty(ClassificationModel.CLASSIFICATION, text);

        if (classification == null)
        {
            classification = classificationService.create();
            classification.setEffort(effort);
            classification.setSeverity(severity);
            classification.setDescription(description);
            classification.setClassification(text);

            classification.setRuleID(((Rule) context.get(Rule.class)).getId());

            LinkService linkService = new LinkService(graphContext);
            for (Link link : links)
            {
                LinkModel linkModel = linkService.getOrCreate(link.getTitle(), link.getLink());
                classification.addLink(linkModel);
            }
        }

        classificationService.attachClassification(classification, payload);
        if (payload instanceof SourceFileModel)
            ((SourceFileModel) payload).setGenerateSourceReport(true);
        log.info("Classification added to " + payload.getPrettyPathWithinProject() + " [" + this + "] ");
    }

    protected void setClassificationText(String classification)
    {
        this.classificationPattern = new RegexParameterizedPatternParser(classification);
    }

    @Override
    public String toString()
    {
        StringBuilder result = new StringBuilder();
        result.append("Classification.as(" + classificationPattern.getPattern() + ")");
        if (descriptionPattern != null && !descriptionPattern.getPattern().trim().isEmpty())
            result.append(".withDescription(" + descriptionPattern + ")");
        if (effort != 0)
            result.append(".withEffort(" + effort + ")");
        if (links != null && !links.isEmpty())
            result.append(".with(" + links + ")");
        return result.toString();
    }

    public List<Link> getLinks()
    {
        return links;
    }

    public RegexParameterizedPatternParser getClassificationPattern()
    {
        return classificationPattern;
    }

    public RegexParameterizedPatternParser getDescriptionPattern()
    {
        return descriptionPattern;
    }

    public int getEffort()
    {
        return effort;
    }

    @Override
    public Set<String> getRequiredParameterNames()
    {
        Set<String> result = new HashSet<>(classificationPattern.getRequiredParameterNames());
        if (descriptionPattern != null)
            result.addAll(descriptionPattern.getRequiredParameterNames());
        return result;
    }

    @Override
    public void setParameterStore(ParameterStore store)
    {
        classificationPattern.setParameterStore(store);
        if (descriptionPattern != null)
            descriptionPattern.setParameterStore(store);
    }

}
