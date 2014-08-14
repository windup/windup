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
import org.jboss.windup.reporting.model.FileLocationModel;
import org.jboss.windup.reporting.model.FileReferenceModel;
import org.jboss.windup.reporting.model.LinkModel;
import org.ocpsoft.rewrite.config.Rule;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * Classifies a {@link FileModel} {@link Iteration} payload.
 * 
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public class Classification extends AbstractIterationOperation<FileModel> implements ClassificationBuilder
{
    private List<Link> links = new ArrayList<>();
    private String classificationText;
    private String details;
    private int effort;

    private Classification(String variable)
    {
        super(variable);
    }
    
    private Classification()
    {
        super();
    }
    
    /**
     * Set the payload to the fileModel of the given instance even though the variable is not directly referencing it.
     * This is mainly to simplify the creation of the rule, when the FileModel itself is not being iterated but just a model
     * referencing it.
     * 
     */
    @Override
    @SuppressWarnings("unchecked")
    public void perform(GraphRewrite event, EvaluationContext context)
    {
        checkVariableName(event, context);
        WindupVertexFrame payload = resolveVariable(event, getVariableName());
        if(payload instanceof FileReferenceModel) {
            perform(event, context,((FileReferenceModel)payload).getFile());
        } else {
            perform(event, context);
        }
        
    }

    public static ClassificationBuilder of(String variable)
    {
        return new Classification(variable);
    }

    public Classification withDescription(String details)
    {
        this.details = details;
        return this;
    }

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

    @Override
    public Classification as(String classification)
    {
        this.classificationText = classification;
        return this;
    }

    public static Classification classifyAs(String classification)
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
        GraphService<ClassificationModel> classificationService = new GraphService<>(graphContext,
                    ClassificationModel.class);
        ClassificationModel classification = classificationService.getUniqueByProperty(
                    ClassificationModel.PROPERTY_CLASSIFICATION, classificationText);

        if (classification == null)
        {
            classification = classificationService.create();
            classification.addFileModel(payload);
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
}
