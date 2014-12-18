package org.jboss.windup.graph.wrapper;

import java.util.Iterator;

import com.tinkerpop.blueprints.CloseableIterable;
import com.tinkerpop.blueprints.Edge;

/**
 * A sequence of edges that applies the list of listeners into each edge.
 *
 * @author Stephen Mallette
 */
public class EventEdgeIterable implements CloseableIterable<Edge>
{

    private final Iterable<Edge> iterable;
    private final EventGraphWithMultiThreadedTitanTransactions eventGraph;

    public EventEdgeIterable(final Iterable<Edge> iterable, final EventGraphWithMultiThreadedTitanTransactions eventGraph)
    {
        this.iterable = iterable;
        this.eventGraph = eventGraph;
    }

    public Iterator<Edge> iterator()
    {
        return new Iterator<Edge>()
        {
            private final Iterator<Edge> itty = iterable.iterator();

            public void remove()
            {
                this.itty.remove();
            }

            public Edge next()
            {
                return new EventEdge(this.itty.next(), eventGraph);
            }

            public boolean hasNext()
            {
                return this.itty.hasNext();
            }
        };
    }

    public void close()
    {
        if (this.iterable instanceof CloseableIterable)
        {
            ((CloseableIterable) this.iterable).close();
        }
    }
}
