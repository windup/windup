package org.jboss.windup.graph.wrapper;

import org.jboss.windup.graph.VertexWrapper;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.VertexQuery;
import com.tinkerpop.blueprints.util.wrappers.WrapperVertexQuery;

/**
 * An vertex with a GraphChangedListener attached. Those listeners are notified when changes occur to the properties of the vertex.
 *
 * @author Stephen Mallette
 */
public class EventVertex extends EventElement implements Vertex, VertexWrapper
{
    public EventVertex(final Vertex rawVertex, final EventGraphWithMultiThreadedTitanTransactions eventGraph)
    {
        super(rawVertex, eventGraph);
    }

    public Iterable<Edge> getEdges(final Direction direction, final String... labels)
    {
        super.eventGraph.acquireTxnReadLock();
        try
        {
            return new EventEdgeIterable(((Vertex) getBaseElement()).getEdges(direction, labels), this.eventGraph);
        }
        finally
        {
            super.eventGraph.releaseTxnReadLock();
        }
    }

    public Iterable<Vertex> getVertices(final Direction direction, final String... labels)
    {
        super.eventGraph.acquireTxnReadLock();
        try
        {
            return new EventVertexIterable(((Vertex) getBaseElement()).getVertices(direction, labels), this.eventGraph);
        }
        finally
        {
            super.eventGraph.releaseTxnReadLock();
        }
    }

    public VertexQuery query()
    {
        super.eventGraph.acquireTxnReadLock();
        try
        {
            return new WrapperVertexQuery(((Vertex) getBaseElement()).query())
            {
                @Override
                public Iterable<Vertex> vertices()
                {
                    return new EventVertexIterable(this.query.vertices(), eventGraph);
                }

                @Override
                public Iterable<Edge> edges()
                {
                    return new EventEdgeIterable(this.query.edges(), eventGraph);
                }
            };
        }
        finally
        {
            super.eventGraph.releaseTxnReadLock();
        }
    }

    public Edge addEdge(final String label, final Vertex vertex)
    {
        super.eventGraph.acquireTxnReadLock();
        try
        {
            return this.eventGraph.addEdge(null, this, vertex, label);
        }
        finally
        {
            super.eventGraph.releaseTxnReadLock();
        }
    }

    public Vertex getBaseVertex()
    {
        return (Vertex) getBaseElement();
    }
}
