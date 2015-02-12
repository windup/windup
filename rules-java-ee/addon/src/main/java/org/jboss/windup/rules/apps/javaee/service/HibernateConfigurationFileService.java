package org.jboss.windup.rules.apps.javaee.service;

import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.rules.apps.javaee.model.HibernateConfigurationFileModel;

/**
 * Contains methods for querying, updating, and deleting {@link HibernateConfigurationFileModel}
 * 
 * @author jsightler <jesse.sightler@gmail.com>
 * 
 */
public class HibernateConfigurationFileService extends GraphService<HibernateConfigurationFileModel>
{
    public HibernateConfigurationFileService(GraphContext context)
    {
        super(context, HibernateConfigurationFileModel.class);
    }
}
