package org.jboss.windup.rules.apps.javaee.service;

import javax.inject.Inject;

import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.rules.apps.javaee.model.HibernateMappingFileModel;

/**
 * Contains methods for creating, searching, and deleting {@link HibernateMappingFileModel}s.
 * 
 * @author jsightler <jesse.sightler@gmail.com>
 */
public class HibernateMappingFileService extends GraphService<HibernateMappingFileModel>
{

    public HibernateMappingFileService()
    {
        super(HibernateMappingFileModel.class);
    }

    @Inject
    public HibernateMappingFileService(GraphContext context)
    {
        super(context, HibernateMappingFileModel.class);
    }
}
