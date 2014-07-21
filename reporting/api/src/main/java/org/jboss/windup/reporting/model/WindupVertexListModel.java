package org.jboss.windup.reporting.model;

import org.jboss.windup.graph.model.WindupVertexFrame;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;

public interface WindupVertexListModel extends WindupVertexFrame
{

    @Adjacency(label = "list", direction = Direction.OUT)
    Iterable<WindupVertexFrame> getList();

    @Adjacency(label = "list", direction = Direction.OUT)
    Iterable<WindupVertexFrame> setList(Iterable<WindupVertexFrame> list);

    @Adjacency(label = "list", direction = Direction.OUT)
    void addItem(WindupVertexFrame item);

}
