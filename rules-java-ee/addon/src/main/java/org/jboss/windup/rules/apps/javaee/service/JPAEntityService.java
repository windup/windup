package org.jboss.windup.rules.apps.javaee.service;

import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.rules.apps.javaee.model.JPAEntityModel;

/**
 * Contains methods for querying, updating, and deleting {@link JPAEntityModel}
 *
 * @author <a href="mailto:bradsdavis@gmail.com">Brad Davis</a>
 */
public class JPAEntityService extends GraphService<JPAEntityModel> {
    public JPAEntityService(GraphContext context) {
        super(context, JPAEntityModel.class);
    }
}
