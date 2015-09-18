package org.jboss.windup.rules.apps.javaee.model;

import org.jboss.windup.graph.model.WindupVertexFrame;

import com.tinkerpop.frames.modules.typedgraph.TypeValue;

/**
 * Marker / base interface for Remote Services.
 * 
 * @author <a href="mailto:bradsdavis@gmail.com">Brad Davis</a>
 */
@TypeValue(RemoteServiceModel.TYPE)
public interface RemoteServiceModel extends WindupVertexFrame
{
    String TYPE = "RemoteService";
}
