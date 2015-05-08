package org.jboss.windup.project.operation;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.operation.iteration.AbstractIterationOperation;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.reporting.model.OverviewReportLineMessageModel;
import org.ocpsoft.rewrite.config.Rule;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * Provides the message that will be reported on the project overview page.
 * 
 * @author <a href="mailto:mbriskar@gmail.com">Matej Briskar</a>
 *
 */
public class LineItem extends AbstractIterationOperation<ProjectModel>
{

    private String message;

    LineItem(String variable)
    {
        super(variable);
    }

    LineItem()
    {
        super();
    }

    public static LineItem withMessage(String text)
    {
        LineItem lineItem = new LineItem();
        lineItem.setMessage(text);
        return lineItem;
    }

    @Override
    public void perform(GraphRewrite event, EvaluationContext context, ProjectModel payload)
    {
        GraphContext graphContext = event.getGraphContext();
        GraphService<OverviewReportLineMessageModel> overviewLineService = new GraphService<>(graphContext, OverviewReportLineMessageModel.class);
        OverviewReportLineMessageModel overviewLine = overviewLineService.create();
        overviewLine.setMessage(message);
        overviewLine.setProject(payload);
        overviewLine.setRuleID(((Rule) context.get(Rule.class)).getId());
    }

    public String getMessage()
    {
        return message;
    }

    public void setMessage(String message)
    {
        this.message = message;
    }

}
