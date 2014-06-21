package org.jboss.windup.rules.apps.web.dao.impl;

import java.util.Iterator;

import javax.inject.Singleton;

import org.jboss.windup.rules.apps.web.dao.WebConfigurationDao;
import org.jboss.windup.rules.apps.web.model.WebConfigurationFacetModel;
import org.jboss.windup.graph.model.resource.XmlResourceModel;

import com.thinkaurelius.titan.core.attribute.Text;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.gremlin.java.GremlinPipeline;
import org.jboss.windup.graph.dao.impl.BaseDaoImpl;
import org.jboss.windup.rules.apps.web.dao.WebConfigurationDao;

@Singleton
public class WebConfigurationDaoImpl extends BaseDaoImpl<WebConfigurationFacetModel> implements WebConfigurationDao
{

    public WebConfigurationDaoImpl()
    {
        super(WebConfigurationFacetModel.class);
    }

    public boolean isWebConfiguration(XmlResourceModel resource)
    {
        return (new GremlinPipeline<Vertex, Vertex>(resource.asVertex())).in("xmlFacet").as("facet")
                    .has("type", Text.CONTAINS, this.getTypeValueForSearch()).back("facet").iterator().hasNext();
    }

    public WebConfigurationFacetModel getWebConfigurationFromResource(XmlResourceModel resource)
    {
        @SuppressWarnings("unchecked")
        Iterator<Vertex> v = (Iterator<Vertex>) (new GremlinPipeline<Vertex, Vertex>(resource.asVertex()))
                    .in("xmlFacet").as("facet").has("type", Text.CONTAINS, this.getTypeValueForSearch()).back("facet").iterator();
        if (v.hasNext())
        {
            return getContext().getFramed().frame(v.next(), this.getType());
        }

        return null;
    }
}
