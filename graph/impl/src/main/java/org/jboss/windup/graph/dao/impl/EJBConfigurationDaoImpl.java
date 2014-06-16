package org.jboss.windup.graph.dao.impl;

import java.util.Iterator;

import javax.inject.Singleton;

import org.jboss.windup.rules.apps.ejb.dao.EJBConfigurationDao;
import org.jboss.windup.rules.apps.ejb.model.meta.xml.EjbConfigurationFacetModel;
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
                    .has("type", Text.CONTAINS, this.getTypeValueForSearch()).back("facet").iterator().hasNext();
    }

    public EjbConfigurationFacetModel getEjbConfigurationFromResource(XmlResourceModel resource)
    {
        @SuppressWarnings("unchecked")
        Iterator<Vertex> v = (Iterator<Vertex>) (new GremlinPipeline<Vertex, Vertex>(resource.asVertex()))
                    .in("xmlFacet").as("facet").has("type", Text.CONTAINS, this.getTypeValueForSearch()).back("facet").iterator();
        if (v.hasNext())
        {
            return getContext().getFramed().frame(v.next(), EjbConfigurationFacetModel.class);
        }

        return null;
    }
}
