package org.jboss.windup.rules.apps.javaee.service;

import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.rules.apps.javaee.model.SpringBeanModel;

/**
 * Contains methods for creating, querying, and updating SpringBeanModel entries in the Graph.
 * 
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 * 
 */
public class SpringBeanService extends GraphService<SpringBeanModel>
{
    public SpringBeanService(GraphContext context)
    {
        super(context, SpringBeanModel.class);
    }

    public Iterable<SpringBeanModel> findAllBySpringBeanName(String name)
    {
        return super.findAllByProperty(SpringBeanModel.SPRING_BEAN_NAME, name);
    }
}
