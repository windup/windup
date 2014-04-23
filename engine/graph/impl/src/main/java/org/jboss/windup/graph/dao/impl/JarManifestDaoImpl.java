package org.jboss.windup.graph.dao.impl;

import java.util.Iterator;

import javax.inject.Singleton;

import org.jboss.windup.graph.dao.JarManifestDao;
import org.jboss.windup.graph.model.meta.JarManifest;
import org.jboss.windup.graph.model.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.gremlin.java.GremlinPipeline;

@Singleton
public class JarManifestDaoImpl extends BaseDaoImpl<JarManifest> implements JarManifestDao
{

    private static Logger LOG = LoggerFactory.getLogger(JarManifestDaoImpl.class);

    public JarManifestDaoImpl()
    {
        super(JarManifest.class);
    }

    public boolean isManifestResource(Resource resource)
    {
        return (new GremlinPipeline<Vertex, Vertex>(resource.asVertex())).out("manifestFacet").iterator().hasNext();
    }

    public JarManifest getManifestFromResource(Resource resource)
    {
        Iterator<Vertex> v = (new GremlinPipeline<Vertex, Vertex>(resource.asVertex())).out("manifestFacet").iterator();
        if (v.hasNext())
        {
            return context.getFramed().frame(v.next(), JarManifest.class);
        }

        return null;
    }
}
