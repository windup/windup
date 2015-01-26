package org.jboss.windup.reporting.model;

import org.jboss.windup.graph.model.WindupVertexFrame;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeValue(WindupVertexListModel.TYPE)
public interface WindupVertexListModel extends WindupVertexFrame
{
    public static final String TYPE = "WindupVertexListModel";

    @Adjacency(label = "list", direction = Direction.OUT)
    Iterable<WindupVertexFrame> getList();

    @Adjacency(label = "list", direction = Direction.OUT)
    Iterable<WindupVertexFrame> setList(Iterable<WindupVertexFrame> list);

    @Adjacency(label = "list", direction = Direction.OUT)
    void addItem(WindupVertexFrame item);

}
