package org.jboss.windup.config.gremlinquery;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.graph.model.WindupVertexFrame;

import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;
import com.tinkerpop.gremlin.java.GremlinPipeline;

/**
 * Filters the input by type, using a Gremlin operation that will use indexes if possible.
 */
public class GremlinTypeFilter implements GremlinQueryFilter<Vertex, Vertex>
{
    private Class<? extends WindupVertexFrame> type;

    public GremlinTypeFilter()
    {
    }

    public GremlinTypeFilter(Class<? extends WindupVertexFrame> type)
    {
        this.type = type;
    }

    @SuppressWarnings("unchecked")
    @Override
    public GremlinPipeline<Vertex, Vertex> process(GraphRewrite event, GremlinPipeline<Vertex, Vertex> pipe)
    {
        TypeValue typeValue = this.type.getAnnotation(TypeValue.class);
        if (typeValue == null)
        {
            throw new IllegalArgumentException("Must contain annotation 'TypeValue'");
        }
        return (GremlinPipeline<Vertex, Vertex>) pipe.has(WindupVertexFrame.TYPE_PROP, typeValue.value());
    }

    @Override
    public String toString()
    {
        return "filterByType(" + this.type.getCanonicalName() + ")";
    }
}
