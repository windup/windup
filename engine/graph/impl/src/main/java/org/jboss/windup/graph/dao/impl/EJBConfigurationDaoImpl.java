package org.jboss.windup.graph.dao.impl;

import java.util.Iterator;

import javax.inject.Singleton;

import org.jboss.windup.graph.dao.EJBConfigurationDao;
import org.jboss.windup.graph.model.meta.xml.EjbConfigurationFacet;
import org.jboss.windup.graph.model.resource.XmlResource;

import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.gremlin.java.GremlinPipeline;

@Singleton
public class EJBConfigurationDaoImpl extends BaseDaoImpl<EjbConfigurationFacet> implements EJBConfigurationDao
{
    public EJBConfigurationDaoImpl()
    {
        super(EjbConfigurationFacet.class);
    }

    public boolean isEJBConfiguration(XmlResource resource)
    {
        return (new GremlinPipeline<Vertex, Vertex>(resource.asVertex())).in("xmlFacet").as("facet")
                    .has("type", this.typeValue).back("facet").iterator().hasNext();
    }

    public EjbConfigurationFacet getEjbConfigurationFromResource(XmlResource resource)
    {
        Iterator<Vertex> v = (Iterator<Vertex>) (new GremlinPipeline<Vertex, Vertex>(resource.asVertex()))
                    .in("xmlFacet").as("facet").has("type", this.typeValue).back("facet").iterator();
        if (v.hasNext())
        {
            return context.getFramed().frame(v.next(), EjbConfigurationFacet.class);
        }

        return null;
    }
}
