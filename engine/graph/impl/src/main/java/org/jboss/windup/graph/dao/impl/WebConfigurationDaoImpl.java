package org.jboss.windup.graph.dao.impl;

import java.util.Iterator;

import javax.inject.Singleton;

import org.jboss.windup.graph.dao.WebConfigurationDao;
import org.jboss.windup.graph.model.meta.xml.WebConfigurationFacet;
import org.jboss.windup.graph.model.resource.XmlResource;

import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.gremlin.java.GremlinPipeline;

@Singleton
public class WebConfigurationDaoImpl extends BaseDaoImpl<WebConfigurationFacet> implements WebConfigurationDao
{

    public WebConfigurationDaoImpl()
    {
        super(WebConfigurationFacet.class);
    }

    public boolean isWebConfiguration(XmlResource resource)
    {
        return (new GremlinPipeline<Vertex, Vertex>(resource.asVertex())).in("xmlFacet").as("facet")
                    .has("type", this.typeValue).back("facet").iterator().hasNext();
    }

    public WebConfigurationFacet getWebConfigurationFromResource(XmlResource resource)
    {
        Iterator<Vertex> v = (Iterator<Vertex>) (new GremlinPipeline<Vertex, Vertex>(resource.asVertex()))
                    .in("xmlFacet").as("facet").has("type", this.typeValue).back("facet").iterator();
        if (v.hasNext())
        {
            return context.getFramed().frame(v.next(), this.type);
        }

        return null;
    }
}
