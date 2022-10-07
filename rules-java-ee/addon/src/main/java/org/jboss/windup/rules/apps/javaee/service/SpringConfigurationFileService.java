package org.jboss.windup.rules.apps.javaee.service;

import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.rules.apps.javaee.model.SpringConfigurationFileModel;

/**
 * Provides methods for creating, updating, and querying {@link SpringConfigurationFileModel}s.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class SpringConfigurationFileService extends GraphService<SpringConfigurationFileModel> {
    public SpringConfigurationFileService(GraphContext context) {
        super(context, SpringConfigurationFileModel.class);
    }
}
