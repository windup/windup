package org.jboss.windup.reporting.service;

import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.reporting.model.InlineHintModel;

/**
 * This provides helper functions for finding and creating BlackListModels within the graph.
 * 
 * @author jsightler <jesse.sightler@gmail.com>
 * 
 */
public class InlineHintService extends GraphService<InlineHintModel>
{

    public InlineHintService()
    {
        super(InlineHintModel.class);
    }

    public InlineHintService(GraphContext context)
    {
        super(context, InlineHintModel.class);
    }
}
