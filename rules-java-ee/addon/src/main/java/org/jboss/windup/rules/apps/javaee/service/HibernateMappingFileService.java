package org.jboss.windup.rules.apps.javaee.service;

import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.rules.apps.javaee.model.HibernateMappingFileModel;

/**
 * Contains methods for creating, searching, and deleting {@link HibernateMappingFileModel}s.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class HibernateMappingFileService extends GraphService<HibernateMappingFileModel> {
    public HibernateMappingFileService(GraphContext context) {
        super(context, HibernateMappingFileModel.class);
    }
}
