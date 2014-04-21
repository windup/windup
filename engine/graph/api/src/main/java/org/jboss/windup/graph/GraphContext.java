package org.jboss.windup.graph;

import com.thinkaurelius.titan.core.TitanGraph;
import com.tinkerpop.frames.FramedGraph;

public interface GraphContext
{
    public TitanGraph getGraph();
    public FramedGraph<TitanGraph> getFramed();
}
