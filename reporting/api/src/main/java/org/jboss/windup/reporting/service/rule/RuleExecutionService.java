package org.jboss.windup.reporting.service.rule;

import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.reporting.model.rule.RuleExecutionModel;

/**
 * @author <a href="mailto:dklingenberg@gmail.com">David Klingenberg</a>
 */
public class RuleExecutionService extends GraphService<RuleExecutionModel> {
    public RuleExecutionService(GraphContext context) {
        super(context, RuleExecutionModel.class);
    }
}
