package org.jboss.windup.rules.apps.javaee.service;

import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.rules.apps.javaee.model.JmsConnectionFactoryModel;

/**
 * Contains methods for querying, updating, and deleting {@link JmsConnectionFactoryModel}
 *
 * @author <a href="mailto:bradsdavis@gmail.com">Brad Davis</a>
 */
public class JmsConnectionFactoryService extends GraphService<JmsConnectionFactoryModel> {
    public JmsConnectionFactoryService(GraphContext context) {
        super(context, JmsConnectionFactoryModel.class);
    }

}
