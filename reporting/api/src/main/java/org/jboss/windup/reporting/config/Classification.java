package org.jboss.windup.reporting.config;

import java.util.ArrayList;
import java.util.List;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.config.operation.ruleelement.AbstractIterationOperation;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.reporting.model.ClassificationModel;
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
        super(FileModel.class, variable);
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

    @Override
    public void perform(GraphRewrite event, EvaluationContext context, FileModel payload)
    {

        GraphService<ClassificationModel> classificationService = new GraphService<>(event.getGraphContext(),
                    ClassificationModel.class);
        ClassificationModel classification = classificationService.create();
        classification.setFileModel(payload);
        classification.setEffort(effort);
        classification.setDescription(details);
        classification.setClassifiation(classificationText);

        // TODO replace this with a link to a RuleModel, once that is implemented.
        classification.setRuleID(((Rule) event.getRewriteContext().get(Rule.class)).getId());

        GraphService<LinkModel> linkService = new GraphService<>(event.getGraphContext(), LinkModel.class);
        for (Link link : links)
        {
            LinkModel linkModel = linkService.create();
            linkModel.setDescription(link.getDescription());
            linkModel.setLink(link.getLink());
            classification.addLink(linkModel);
        }
    }

}
