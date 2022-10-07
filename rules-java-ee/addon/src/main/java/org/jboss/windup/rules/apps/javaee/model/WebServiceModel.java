package org.jboss.windup.rules.apps.javaee.model;

import org.jboss.windup.graph.model.TypeValue;

/**
 * Base type for Web Service.
 *
 * @author <a href="mailto:bradsdavis@gmail.com">Brad Davis</a>
 */
@TypeValue(WebServiceModel.TYPE)
public interface WebServiceModel extends RemoteServiceModel {
    String TYPE = "WebServiceModel";
}
