package org.jboss.windup.reporting.model.association;

import org.jboss.windup.graph.model.LinkModel;
import org.jboss.windup.graph.model.TypeValue;
import org.jboss.windup.graph.model.WindupVertexFrame;

import org.apache.tinkerpop.gremlin.structure.Direction;
import org.jboss.windup.graph.Adjacency;

import java.util.List;

@TypeValue(LinkableModel.TYPE)
public interface LinkableModel extends WindupVertexFrame {
    String TYPE = "LinkableModel";
    String LINK = "linkable";

    /**
     * Contains the link for the resource.
     */
    @Adjacency(label = LINK, direction = Direction.OUT)
    List<LinkModel> getLinks();

    /**
     * Contains the link for the resource.
     */
    @Adjacency(label = LINK, direction = Direction.OUT)
    void addLink(LinkModel link);
}
