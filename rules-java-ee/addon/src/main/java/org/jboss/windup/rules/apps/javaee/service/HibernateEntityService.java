package org.jboss.windup.rules.apps.javaee.service;

import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.rules.apps.javaee.model.HibernateEntityModel;

/**
 * Contains methods for querying, creating, and deleting {@link HibernateEntityModel}s.
 * 
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 * 
 */
public class HibernateEntityService extends GraphService<HibernateEntityModel>
{
    public HibernateEntityService(GraphContext context)
    {
        super(context, HibernateEntityModel.class);
    }
}
