package org.jboss.windup.config.query;

import java.util.List;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.graph.model.WindupVertexFrame;

import com.tinkerpop.blueprints.Predicate;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import com.tinkerpop.frames.FramedGraphQuery;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.jboss.windup.graph.frames.TypeAwareFramedGraphQuery;

class QueryTypeCriterion implements QueryFramesCriterion, QueryGremlinCriterion
{
    private final String typeValue;
    private final Class<? extends WindupVertexFrame> searchedClass;

    public QueryTypeCriterion(Class<? extends WindupVertexFrame> clazz)
    {
        this.searchedClass = clazz;
        this.typeValue = TypeAwareFramedGraphQuery.getTypeValue(clazz);
    }

    @Override
    public void query(FramedGraphQuery q)
    {
        q.has(WindupVertexFrame.TYPE_PROP, typeValue);
    }


    /**
     * Adds a criterion to given pipeline which filters out vertices representing given WindupVertexFrame.
     */
    public static GraphTraversal<Vertex, Vertex> addPipeFor(GraphTraversal<Vertex, Vertex> pipeline,
                Class<? extends WindupVertexFrame> clazz)
    {
        pipeline.has(WindupVertexFrame.TYPE_PROP, TypeAwareFramedGraphQuery.getTypeValue(clazz));
        return pipeline;
    }

    public String toString()
    {
        return ".formType(" + searchedClass.getSimpleName() + ")";
    }

    @Override
    public void query(GraphRewrite event, GraphTraversal<Vertex, Vertex> pipeline)
    {
        pipeline.has(WindupVertexFrame.TYPE_PROP, new Predicate()
        {

            @Override
            public boolean evaluate(Object first, Object second)
            {
                @SuppressWarnings("unchecked")
                List<String> firstList = (List<String>) first;
                return firstList.contains(second);
            }

        }, typeValue);
    }
}
