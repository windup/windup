package org.jboss.windup.graph;

import java.io.File;

import org.jboss.windup.graph.typedgraph.GraphTypeRegistry;

import com.thinkaurelius.titan.core.TitanGraph;
import com.tinkerpop.frames.FramedGraph;

public interface GraphContext
{
    public TitanGraph getGraph();

    public FramedGraph<TitanGraph> getFramed();

    public GraphTypeRegistry getGraphTypeRegistry();

    public File getDiskCacheDirectory();
}
