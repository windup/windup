package org.jboss.windup.config.query;

import com.thinkaurelius.titan.core.attribute.Text;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.frames.FramedGraphQuery;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;
import com.tinkerpop.gremlin.java.GremlinPipeline;
import org.jboss.windup.graph.model.WindupVertexFrame;
import static org.jboss.windup.graph.model.WindupVertexFrame.TYPE_PROP;

class QueryTypeCriterion implements QueryFramesCriterion
{
    private String typeValue;

    public QueryTypeCriterion(Class<? extends WindupVertexFrame> clazz)
    {
        this.typeValue = getTypeValue( clazz );
    }
    
    @Override
    public void query(FramedGraphQuery q)
    {
        q.has(TYPE_PROP, Text.CONTAINS, typeValue);
    }
    
    private static String getTypeValue( Class<? extends WindupVertexFrame> clazz ) {
        TypeValue typeValueAnnotation = clazz.getAnnotation(TypeValue.class);
        if (typeValueAnnotation == null)
        {
            throw new IllegalArgumentException("Class " + clazz.getCanonicalName() + " lacks a @TypeValue annotation");
        }
        else
        {
            return typeValueAnnotation.value();
        }
    }

    /**
     *  Adds a criterion to given pipeline which filters out vertices representing given WindupVertexFrame.
     */
    public static GremlinPipeline<Vertex, Vertex> addPipeFor( GremlinPipeline<Vertex, Vertex> pipeline, Class<? extends WindupVertexFrame> clazz ){
        pipeline.has(TYPE_PROP, Text.CONTAINS, getTypeValue( clazz ));
        return pipeline;
    }
}