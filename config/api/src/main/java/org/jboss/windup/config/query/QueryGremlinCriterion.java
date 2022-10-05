package org.jboss.windup.config.query;

import org.jboss.windup.config.GraphRewrite;

import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public interface QueryGremlinCriterion {
    void query(GraphRewrite event, GraphTraversal<?, Vertex> pipeline);
}
