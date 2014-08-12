package org.jboss.windup.graph.dao;

import java.util.Iterator;

import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ArchiveModel;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.model.resource.ResourceModel;
import org.jboss.windup.graph.service.GraphService;

import com.thinkaurelius.titan.core.attribute.Cmp;
import com.thinkaurelius.titan.core.attribute.Text;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.gremlin.java.GremlinPipeline;
import com.tinkerpop.pipes.PipeFunction;

public class ArchiveService extends GraphService<ArchiveModel>
{
    public ArchiveService()
    {
        super(ArchiveModel.class);
    }

    public ArchiveService(GraphContext context)
    {
        super(context, ArchiveModel.class);
    }

    public Iterable<ArchiveModel> findAllRootArchives()
    {
        // iterate through all vertices
        Iterable<Vertex> pipeline = new GremlinPipeline<Vertex, Vertex>(getGraphContext()
                    .getGraph().query().has(WindupVertexFrame.TYPE_PROP, Cmp.EQUAL, getTypeValueForSearch())
                    .vertices())

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
        return getGraphContext().getFramed().frameVertices(pipeline, ArchiveModel.class);
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
            return getGraphContext().getFramed().frame(v.next(), ArchiveModel.class);
        }

        return null;
    }
}
