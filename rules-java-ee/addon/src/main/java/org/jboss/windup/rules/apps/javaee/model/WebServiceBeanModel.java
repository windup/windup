package org.jboss.windup.rules.apps.javaee.model;

import org.jboss.windup.graph.model.WindupVertexFrame;

import com.tinkerpop.frames.modules.typedgraph.TypeValue;

/**
 * Base type for Web Service.
 * 
 * @author <a href="mailto:bradsdavis@gmail.com">Brad Davis</a>
 */
@TypeValue(WebServiceBeanModel.TYPE)
public interface WebServiceBeanModel extends WindupVertexFrame
{
    public static final String TYPE = "WebServiceBean";
}
