package org.jboss.windup.graph.dao.impl;

import java.util.Iterator;

import javax.inject.Singleton;

import org.jboss.windup.rules.apps.java.scan.dao.JarManifestDao;
import org.jboss.windup.rules.apps.java.scan.model.JarManifestModel;
import org.jboss.windup.graph.model.resource.ResourceModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.gremlin.java.GremlinPipeline;

@Singleton
public class JarManifestDaoImpl extends BaseDaoImpl<JarManifestModel> implements JarManifestDao
{

    private static Logger LOG = LoggerFactory.getLogger(JarManifestDaoImpl.class);

    public JarManifestDaoImpl()
    {
        super(JarManifestModel.class);
    }

    public boolean isManifestResource(ResourceModel resource)
    {
        return (new GremlinPipeline<Vertex, Vertex>(resource.asVertex())).out("manifestFacet").iterator().hasNext();
    }

    public JarManifestModel getManifestFromResource(ResourceModel resource)
    {
        Iterator<Vertex> v = (new GremlinPipeline<Vertex, Vertex>(resource.asVertex())).out("manifestFacet").iterator();
        if (v.hasNext())
        {
            return getContext().getFramed().frame(v.next(), JarManifestModel.class);
        }

        return null;
    }
}
