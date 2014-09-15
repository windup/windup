package org.jboss.windup.rules.apps.javaee.service;

import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.rules.apps.javaee.model.SpringBeanModel;

/**
 * Contains methods for creating, querying, and updating SpringBeanModel entries in the Graph.
 * 
 * @author jsightler <jesse.sightler@gmail.com>
 * 
 */
public class SpringBeanService extends GraphService<SpringBeanModel>
{
    public SpringBeanService(GraphContext context)
    {
        super(context, SpringBeanModel.class);
    }

}
