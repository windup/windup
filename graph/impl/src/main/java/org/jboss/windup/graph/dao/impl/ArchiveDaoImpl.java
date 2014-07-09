package org.jboss.windup.graph.dao.impl;

import java.util.Iterator;

import javax.inject.Singleton;

import org.jboss.windup.graph.dao.ArchiveDao;
import org.jboss.windup.graph.dao.BaseDaoImpl;
import org.jboss.windup.graph.model.ArchiveModel;
import org.jboss.windup.graph.model.resource.ResourceModel;

import com.thinkaurelius.titan.core.attribute.Text;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.gremlin.java.GremlinPipeline;
import com.tinkerpop.pipes.PipeFunction;

@Singleton
public class ArchiveDaoImpl extends BaseDaoImpl<ArchiveModel> implements ArchiveDao
{
    public ArchiveDaoImpl()
    {
        super(ArchiveModel.class);
    }

    public Iterable<ArchiveModel> findAllRootArchives()
    {
        // iterate through all vertices
        Iterable<Vertex> pipeline = new GremlinPipeline<Vertex, Vertex>(getContext()
                    .getGraph().query().has("type", Text.CONTAINS, getTypeValueForSearch()).vertices())

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
        return getContext().getFramed().frameVertices(pipeline, ArchiveModel.class);
    }

    public boolean isArchiveResource(ResourceModel resource)
    {
        return (new GremlinPipeline<Vertex, Vertex>(resource.asVertex())).out("archiveResourceFacet").iterator()
                    .hasNext();
    }

    public ArchiveModel getArchiveFromResource(ResourceModel resource)
    {
        Iterator<Vertex> v = (new GremlinPipeline<Vertex, Vertex>(resource.asVertex())).out("archiveResourceFacet")
                    .iterator();
        if (v.hasNext())
        {
            return getContext().getFramed().frame(v.next(), ArchiveModel.class);
        }

        return null;
    }
}
