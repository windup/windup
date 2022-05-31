package org.jboss.windup.config.query;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.jboss.windup.config.GraphRewrite;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public interface QueryGremlinCriterion {
    void query(GraphRewrite event, GraphTraversal<?, Vertex> pipeline);
}
