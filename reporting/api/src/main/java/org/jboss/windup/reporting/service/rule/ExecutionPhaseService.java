package org.jboss.windup.reporting.service.rule;

import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.reporting.model.rule.ExecutionPhaseModel;

/**
 * @author <a href="mailto:dklingenberg@gmail.com">David Klingenberg</a>
 */
public class ExecutionPhaseService extends GraphService<ExecutionPhaseModel> {
    public ExecutionPhaseService(GraphContext context) {
        super(context, ExecutionPhaseModel.class);
    }
}
