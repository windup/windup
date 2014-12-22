package org.jboss.windup.project.operation;

import java.util.logging.Logger;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.operation.ruleelement.AbstractIterationOperation;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.reporting.model.OverviewReportLineMessageModel;
import org.jboss.windup.util.Logging;
import org.ocpsoft.rewrite.config.Rule;
import org.ocpsoft.rewrite.context.EvaluationContext;

public class LineItem extends AbstractIterationOperation<ProjectModel>
{

    private static final Logger LOG = Logging.get(LineItem.class);
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
        GraphService<OverviewReportLineMessageModel> overviewLineService = new GraphService<OverviewReportLineMessageModel>(graphContext,
                    OverviewReportLineMessageModel.class);
        OverviewReportLineMessageModel overviewLine = overviewLineService.create();
        overviewLine.setMessage(message);
        overviewLine.setProject(payload);
        // TODO replace this with a link to a RuleModel, once that is implemented.
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
