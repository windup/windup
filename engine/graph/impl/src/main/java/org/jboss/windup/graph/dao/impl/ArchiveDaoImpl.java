package org.jboss.windup.graph.dao.impl;

import java.util.Iterator;

import javax.inject.Singleton;

import org.jboss.windup.graph.dao.ArchiveDao;
import org.jboss.windup.graph.model.resource.ArchiveResource;
import org.jboss.windup.graph.model.resource.Resource;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.gremlin.java.GremlinPipeline;
import com.tinkerpop.pipes.PipeFunction;

@Singleton
public class ArchiveDaoImpl extends BaseDaoImpl<ArchiveResource> implements ArchiveDao
{
    public ArchiveDaoImpl()
    {
        super(ArchiveResource.class);
    }

    public Iterable<ArchiveResource> findAllRootArchives()
    {
        // iterate through all vertices
        Iterable<Vertex> pipeline = new GremlinPipeline<Vertex, Vertex>(context
                    .getGraph().getVertices("type", typeValue))

                    // check to see whether there is an edge coming in that links to the resource providing the java
                    // class model.
                    .filter(new PipeFunction<Vertex, Boolean>()
                    {
                        public Boolean compute(Vertex argument)
                        {
                            Iterator<Edge> edges = argument.getEdges(Direction.IN, "child").iterator();
                            if (!edges.hasNext())
                            {
                                return true;
                            }
                            // if there aren't two edges, return false.
                            return false;
                        }
                    });
        return context.getFramed().frameVertices(pipeline, ArchiveResource.class);
    }

    public boolean isArchiveResource(Resource resource)
    {
        return (new GremlinPipeline<Vertex, Vertex>(resource.asVertex())).out("archiveResourceFacet").iterator()
                    .hasNext();
    }

    public ArchiveResource getArchiveFromResource(Resource resource)
    {
        Iterator<Vertex> v = (new GremlinPipeline<Vertex, Vertex>(resource.asVertex())).out("archiveResourceFacet")
                    .iterator();
        if (v.hasNext())
        {
            return context.getFramed().frame(v.next(), ArchiveResource.class);
        }

        return null;
    }
}
