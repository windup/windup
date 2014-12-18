package org.jboss.windup.graph.wrapper;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;

/**
 * An edge with a GraphChangedListener attached. Those listeners are notified when changes occur to the properties of the edge.
 *
 * @author Stephen Mallette
 */
public class EventEdge extends EventElement implements Edge
{

    public EventEdge(final Edge rawEdge, final EventGraphWithMultiThreadedTitanTransactions eventGraph)
    {
        super(rawEdge, eventGraph);
    }

    public Vertex getVertex(final Direction direction) throws IllegalArgumentException
    {
        super.eventGraph.acquireTxnReadLock();
        try
        {
            return new EventVertex(this.getBaseEdge().getVertex(direction), this.eventGraph);
        }
        finally
        {
            super.eventGraph.releaseTxnReadLock();
        }
    }

    public String getLabel()
    {
        super.eventGraph.acquireTxnReadLock();
        try
        {
            return ((Edge) this.getBaseElement()).getLabel();
        }
        finally
        {
            super.eventGraph.releaseTxnReadLock();
        }
    }

    public Edge getBaseEdge()
    {
        return (Edge) this.getBaseElement();
    }
}
