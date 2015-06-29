package org.jboss.windup.rules.apps.javaee.model;

import com.tinkerpop.frames.modules.typedgraph.TypeValue;

/**
 * Base type for Web Service.
 * 
 * @author <a href="mailto:bradsdavis@gmail.com">Brad Davis</a>
 */
@TypeValue(WebServiceModel.TYPE)
public interface WebServiceModel extends RemoteServiceModel
{
    public static final String TYPE = "WebService";
}
