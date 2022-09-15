package org.jboss.windup.rules.apps.javaee.service;

import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.rules.apps.javaee.model.JPAConfigurationFileModel;

/**
 * Contains methods for querying, updating, and deleting {@link JPAConfigurationFileModel}
 *
 * @author <a href="mailto:bradsdavis@gmail.com">Brad Davis</a>
 */
public class JPAConfigurationFileService extends GraphService<JPAConfigurationFileModel> {
    public JPAConfigurationFileService(GraphContext context) {
        super(context, JPAConfigurationFileModel.class);
    }
}
