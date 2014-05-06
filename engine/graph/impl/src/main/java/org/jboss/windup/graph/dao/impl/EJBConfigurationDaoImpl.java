package org.jboss.windup.graph.dao.impl;

import java.util.Iterator;

import javax.inject.Singleton;

import org.jboss.windup.graph.dao.EJBConfigurationDao;
import org.jboss.windup.graph.model.meta.xml.EjbConfigurationFacetModel;
import org.jboss.windup.graph.model.resource.XmlResourceModel;

import com.thinkaurelius.titan.core.attribute.Text;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.gremlin.java.GremlinPipeline;

@Singleton
public class EJBConfigurationDaoImpl extends BaseDaoImpl<EjbConfigurationFacetModel> implements EJBConfigurationDao
{
    public EJBConfigurationDaoImpl()
    {
        super(EjbConfigurationFacetModel.class);
    }

    public boolean isEJBConfiguration(XmlResourceModel resource)
    {
        return (new GremlinPipeline<Vertex, Vertex>(resource.asVertex())).in("xmlFacet").as("facet")
                    .has("type", Text.CONTAINS, this.typeValueForSearch).back("facet").iterator().hasNext();
    }

    public EjbConfigurationFacetModel getEjbConfigurationFromResource(XmlResourceModel resource)
    {
        @SuppressWarnings("unchecked")
        Iterator<Vertex> v = (Iterator<Vertex>) (new GremlinPipeline<Vertex, Vertex>(resource.asVertex()))
                    .in("xmlFacet").as("facet").has("type", Text.CONTAINS, this.typeValueForSearch).back("facet").iterator();
        if (v.hasNext())
        {
            return context.getFramed().frame(v.next(), EjbConfigurationFacetModel.class);
        }

        return null;
    }
}
