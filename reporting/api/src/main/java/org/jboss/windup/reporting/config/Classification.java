package org.jboss.windup.reporting.config;

import java.util.ArrayList;
import java.util.List;

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
import org.ocpsoft.rewrite.config.Rule;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * Classifies a {@link FileModel} {@link Iteration} payload.
 * 
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public class Classification extends AbstractIterationOperation<FileModel>
{
    private List<Link> links = new ArrayList<>();
    private String classificationText;
    private String details;
    private int effort;

    Classification(String variable)
    {
        super(variable);
    }

    Classification()
    {
        super();
    }

    /**
     * Set the payload to the fileModel of the given instance even though the variable is not directly referencing it.
     * This is mainly to simplify the creation of the rule, when the FileModel itself is not being iterated but just a
     * model referencing it.
     * 
     */
    @Override
    public void perform(GraphRewrite event, EvaluationContext context)
    {
        checkVariableName(event, context);
        WindupVertexFrame payload = resolveVariable(event, getVariableName());
        if (payload instanceof FileReferenceModel)
        {
            perform(event, context, ((FileReferenceModel) payload).getFile());
        }
        else
        {
            super.perform(event, context);
        }

    }

    /**
     * Create a new classification for the given ref.
     */
    public static ClassificationBuilderOf of(String variable)
    {
        return new ClassificationBuilderOf(variable);
    }

    /**
     * Set the description of this {@link Classification}.
     */
    public Classification withDescription(String details)
    {
        this.details = details;
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
        Classification classif = new Classification();
        classif.classificationText = classification;
        return classif;
    }

    @Override
    public void perform(GraphRewrite event, EvaluationContext context, FileModel payload)
    {
        /*
         * Check for duplicate classifications before we do anything. If a classification already exists, then we don't
         * want to add another.
         */
        GraphContext graphContext = event.getGraphContext();
        GraphService<ClassificationModel> classificationService =  new GraphService<ClassificationModel>(graphContext,
                    ClassificationModel.class);
        ClassificationModel classification = classificationService.getUniqueByProperty(
                    ClassificationModel.PROPERTY_CLASSIFICATION, classificationText);

        if (classification == null)
        {
            classification = classificationService.create();
            classification.setEffort(effort);
            classification.setDescription(details);
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

        classification.addFileModel(payload);
    }

    protected void setClassificationText(String classification)
    {
        this.classificationText = classification;
    }

}
