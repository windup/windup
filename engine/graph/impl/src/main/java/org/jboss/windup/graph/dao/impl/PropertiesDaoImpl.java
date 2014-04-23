package org.jboss.windup.graph.dao.impl;

import java.util.Iterator;

import org.jboss.windup.graph.dao.PropertiesDao;
import org.jboss.windup.graph.model.meta.PropertiesMeta;
import org.jboss.windup.graph.model.resource.Resource;

import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.gremlin.java.GremlinPipeline;

public class PropertiesDaoImpl extends BaseDaoImpl<PropertiesMeta> implements PropertiesDao
{
    public PropertiesDaoImpl()
    {
        super(PropertiesMeta.class);
    }

    public boolean isPropertiesResource(Resource resource)
    {
        return (new GremlinPipeline<Vertex, Vertex>(resource.asVertex())).out("propertiesFacet").iterator().hasNext();
    }

    public PropertiesMeta getPropertiesFromResource(Resource resource)
    {
        Iterator<Vertex> v = (new GremlinPipeline<Vertex, Vertex>(resource.asVertex())).out("propertiesFacet")
                    .iterator();
        if (v.hasNext())
        {
            return context.getFramed().frame(v.next(), PropertiesMeta.class);
        }

        return null;
    }
}
