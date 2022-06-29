package org.jboss.windup.rules.apps.javaee.service;

import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.rules.apps.javaee.model.JPAPersistenceUnitModel;

/**
 * Contains methods for querying, updating, and deleting {@link JPAPersistenceUnitModel}
 *
 * @author <a href="mailto:bradsdavis@gmail.com">Brad Davis</a>
 */
public class JPAPersistenceUnitService extends GraphService<JPAPersistenceUnitModel> {
    public JPAPersistenceUnitService(GraphContext context) {
        super(context, JPAPersistenceUnitModel.class);
    }
}
