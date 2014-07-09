package org.jboss.windup.config.query;

import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.gremlin.java.GremlinPipeline;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public interface QueryGremlinCriterion
{
    public void query(GremlinPipeline<Vertex, Vertex> pipeline);
}
