package org.jboss.windup.config.graphsearch;

import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.gremlin.java.GremlinPipeline;

/**
 * Used to gain access to the underlying GremlinPipeline for advanced query functionality.
 * 
 * @author jsight <jesse.sightler@gmail.com>
 * 
 */
public interface GremlinPipelineCriterion
{
    void configurePipeline(GremlinPipeline<Vertex, Vertex> pipeline);
}
