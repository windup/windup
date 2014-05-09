package org.jboss.windup.addon.config.graphsearch;

import org.jboss.windup.graph.model.meta.WindupVertexFrame;

import com.thinkaurelius.titan.core.attribute.Text;
import com.tinkerpop.frames.FramedGraphQuery;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

class GraphSearchCriterionType implements GraphSearchCriterion
{
    String typeValue;

    public GraphSearchCriterionType(Class<? extends WindupVertexFrame> clazz)
    {
        TypeValue typeValueAnnotation = clazz.getAnnotation(TypeValue.class);
        if (typeValueAnnotation == null)
        {
            throw new IllegalArgumentException("Class " + clazz.getCanonicalName() + " lacks a @TypeValue annotation");
        }
        else
        {
            this.typeValue = typeValueAnnotation.value();
        }
    }

    @Override
    public void query(FramedGraphQuery q)
    {
        q.has("type", Text.CONTAINS, typeValue);
    }
}