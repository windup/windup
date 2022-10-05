package org.jboss.windup.rules.apps.javaee.service;

import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.rules.apps.javaee.model.WebXmlModel;

/**
 * Provides querying, creation, and related methods for accessing {@link WebXmlModel} objects.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class WebXmlService extends GraphService<WebXmlModel> {
    public WebXmlService(GraphContext context) {
        super(context, WebXmlModel.class);
    }
}
