package org.jboss.windup.graph.dao.impl;

import java.util.Iterator;

import org.jboss.windup.graph.dao.PropertiesDao;
import org.jboss.windup.graph.model.meta.PropertiesMetaModel;
import org.jboss.windup.graph.model.resource.ResourceModel;

import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.gremlin.java.GremlinPipeline;

public class PropertiesDaoImpl extends BaseDaoImpl<PropertiesMetaModel> implements PropertiesDao
{
    public PropertiesDaoImpl()
    {
        super(PropertiesMetaModel.class);
    }

    public boolean isPropertiesResource(ResourceModel resource)
    {
        return (new GremlinPipeline<Vertex, Vertex>(resource.asVertex())).out("propertiesFacet").iterator().hasNext();
    }

    public PropertiesMetaModel getPropertiesFromResource(ResourceModel resource)
    {
        Iterator<Vertex> v = (new GremlinPipeline<Vertex, Vertex>(resource.asVertex())).out("propertiesFacet")
                    .iterator();
        if (v.hasNext())
        {
            return context.getFramed().frame(v.next(), PropertiesMetaModel.class);
        }

        return null;
    }
}
