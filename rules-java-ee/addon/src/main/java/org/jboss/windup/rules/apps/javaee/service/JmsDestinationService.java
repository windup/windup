package org.jboss.windup.rules.apps.javaee.service;

import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.rules.apps.javaee.model.DataSourceModel;
import org.jboss.windup.rules.apps.javaee.model.JmsDestinationModel;

/**
 * Contains methods for querying, updating, and deleting {@link JmsDestinationModel}
 * 
 * @author <a href="mailto:bradsdavis@gmail.com">Brad Davis</a>
 * 
 */
public class JmsDestinationService extends GraphService<JmsDestinationModel>
{
    public JmsDestinationService(GraphContext context)
    {
        super(context, JmsDestinationModel.class);
    }
   
}
