package org.jboss.windup.rules.files.condition;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.condition.GraphCondition;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.service.GraphService;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * A {@link GraphCondition} returning true/false based on the fact if the current processing is online/offline.
 */
public class ProcessingIsOnlineGraphCondition extends GraphCondition {
    @Override
    public boolean evaluate(GraphRewrite event, EvaluationContext context) {
        GraphService<WindupConfigurationModel> service = new GraphService<>(event.getGraphContext(), WindupConfigurationModel.class);
        final WindupConfigurationModel windupConfiguration = service.findAll().iterator().next();
        return windupConfiguration.isOnlineMode();
    }
}
