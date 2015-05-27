package org.jboss.windup.reporting.model.association;

import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.reporting.model.LinkModel;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeValue(LinkableModel.TYPE)
public interface LinkableModel extends WindupVertexFrame
{
    public static final String TYPE = "LinkableModel";
    public static final String LINK = "linkable";
    
    /**
     * Contains the link for the resource.
     */
    @Adjacency(label = LINK, direction = Direction.OUT)
    public Iterable<LinkModel> getLinks();

    /**
     * Contains the link for the resource.
     */
    @Adjacency(label = LINK, direction = Direction.OUT)
    public void addLink(LinkModel link);
}
