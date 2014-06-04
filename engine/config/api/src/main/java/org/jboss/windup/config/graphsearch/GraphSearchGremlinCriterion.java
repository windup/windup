package org.jboss.windup.config.graphsearch;

import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.gremlin.java.GremlinPipeline;

public interface GraphSearchGremlinCriterion
{
    public void query(GremlinPipeline<Vertex, Vertex> pipeline);
}
