package org.jboss.windup.rules.apps.javaee.service;

import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.rules.apps.javaee.model.EjbBeanBaseModel;

/**
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class EjbBeanService extends GraphService<EjbBeanBaseModel> {
    public EjbBeanService(GraphContext context) {
        super(context, EjbBeanBaseModel.class);
    }

}
