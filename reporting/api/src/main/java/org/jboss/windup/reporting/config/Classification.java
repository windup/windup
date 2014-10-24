package org.jboss.windup.reporting.config;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.jboss.forge.furnace.util.Assert;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.config.operation.ruleelement.AbstractIterationOperation;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.reporting.model.ClassificationModel;
import org.jboss.windup.reporting.model.FileReferenceModel;
import org.jboss.windup.reporting.model.LinkModel;
import org.jboss.windup.util.exception.WindupException;
import org.ocpsoft.rewrite.config.Rule;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * Classifies a {@link FileModel} {@link Iteration} payload.
 * 
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public class Classification extends AbstractIterationOperation<WindupVertexFrame>
{
    private static final Logger log = Logger.getLogger(Classification.class.getName());

    private List<Link> links = new ArrayList<>();
    private String classificationText;
    private String description;
    private int effort;

    Classification()
    {
        super();
    }

    /**
     * Create a new classification for the given ref.
     */
    public static ClassificationBuilderOf of(String inputVariable)
    {
        return new ClassificationBuilderOf(inputVariable);
    }

    /**
     * Set the description of this {@link Classification}.
     */
    public Classification withDescription(String description)
    {
        this.description = description;
        return this;
    }

    /**
     * Add a {@link Link} to this {@link Classification}.
     */
    public Classification with(Link link)
    {
        this.links.add(link);
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
    public static Classification as(String classification)
    {
        Assert.notNull(classification, "Classification text must not be null.");
        Classification classif = new Classification();
        classif.classificationText = classification;
        return classif;
    }

    @Override
    public void perform(GraphRewrite event, EvaluationContext context, WindupVertexFrame payloadIn)
    {
        FileModel payload;
        if (payloadIn instanceof FileReferenceModel)
        {
            payload = ((FileReferenceModel) payloadIn).getFile();
        }
        else if (payloadIn instanceof FileModel)
        {
            payload = (FileModel) payloadIn;
        }
        else
        {
            throw new WindupException(
                        "Classification attempting to iterate on an item that is neither a FileReferenceModel nor an FileModel... input variable is: "
                                    + getPayloadVariableName());
        }

        /*
         * Check for duplicate classifications before we do anything. If a classification already exists, then we don't want to add another.
         */
        GraphContext graphContext = event.getGraphContext();
        GraphService<ClassificationModel> classificationService = new GraphService<ClassificationModel>(graphContext,
                    ClassificationModel.class);
        ClassificationModel classification = classificationService.getUniqueByProperty(
                    ClassificationModel.PROPERTY_CLASSIFICATION, classificationText);

        if (classification == null)
        {
            classification = classificationService.create();
            classification.setEffort(effort);
            classification.setDescription(description);
            classification.setClassifiation(classificationText);

            // TODO replace this with a link to a RuleModel, once that is implemented.
            classification.setRuleID(((Rule) context.get(Rule.class)).getId());

            GraphService<LinkModel> linkService = new GraphService<>(graphContext, LinkModel.class);
            for (Link link : links)
            {
                LinkModel linkModel = linkService.create();
                linkModel.setDescription(link.getDescription());
                linkModel.setLink(link.getLink());
                classification.addLink(linkModel);
            }
        }

        // check for duplicate adds first
        for (FileModel existingFileModel : classification.getFileModels())
        {
            if (existingFileModel.asVertex().getId().equals(payload.asVertex().getId()))
            {
                log.info("Classification already added to " + payload.getPrettyPathWithinProject() + " [" + this
                            + "] -- not adding again");
                return;
            }
        }
        classification.addFileModel(payload);
        log.info("Classification added to " + payload.getPrettyPathWithinProject() + " [" + this + "] ");
    }

    protected void setClassificationText(String classification)
    {
        this.classificationText = classification;
    }

    @Override
    public String toString()
    {
        StringBuilder result = new StringBuilder();
        result.append("Classification.as(" + classificationText + ")");
        if (description != null && !description.trim().isEmpty())
            result.append(".withDescription(" + description + ")");
        if (effort != 0)
            result.append(".withEffort(" + effort + ")");
        if (links != null && !links.isEmpty())
            result.append(".with(" + links + ")");
        return result.toString();
    }

}
