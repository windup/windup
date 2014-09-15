package org.jboss.windup.rules.apps.javaee.service;

import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.rules.apps.javaee.model.WebXmlModel;

/**
 * 
 * Provides querying, creation, and related methods for accessing {@link WebXmlModel} objects.
 * 
 * @author jsightler <jesse.sightler@gmail.com>
 */
public class WebXmlService extends GraphService<WebXmlModel>
{
    public WebXmlService(GraphContext context)
    {
        super(context, WebXmlModel.class);
    }
}
