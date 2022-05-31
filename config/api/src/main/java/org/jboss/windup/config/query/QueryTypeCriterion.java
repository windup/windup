package org.jboss.windup.config.query;

import com.syncleus.ferma.Traversable;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.graph.GraphTypeManager;
import org.jboss.windup.graph.model.WindupVertexFrame;

import java.util.function.BiPredicate;

class QueryTypeCriterion implements QueryFramesCriterion, QueryGremlinCriterion {
    private final String typeValue;
    private final Class<? extends WindupVertexFrame> searchedClass;

    public QueryTypeCriterion(Class<? extends WindupVertexFrame> clazz) {
        this.searchedClass = clazz;
        this.typeValue = GraphTypeManager.getTypeValue(clazz);
    }

    /**
     * Adds a criterion to given pipeline which filters out vertices representing given WindupVertexFrame.
     */
    public static GraphTraversal<Vertex, Vertex> addPipeFor(GraphTraversal<Vertex, Vertex> pipeline,
                                                            Class<? extends WindupVertexFrame> clazz) {
        pipeline.has(WindupVertexFrame.TYPE_PROP, GraphTypeManager.getTypeValue(clazz));
        return pipeline;
    }

    @Override
    public void query(Traversable<?, ?> q) {
        q.traverse(g -> g.has(WindupVertexFrame.TYPE_PROP, P.eq(typeValue)));
    }

    public String toString() {
        return ".formType(" + searchedClass.getSimpleName() + ")";
    }

    @Override
    @SuppressWarnings("unchecked")
    public void query(GraphRewrite event, GraphTraversal<?, Vertex> pipeline) {
        pipeline.has(WindupVertexFrame.TYPE_PROP, new P(new BiPredicate<String, String>() {
            @Override
            public boolean test(String first, String second) {
                return first.equals(second);
            }
        }, typeValue));
    }
}
