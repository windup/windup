package org.jboss.windup.graph.wrapper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.thinkaurelius.titan.core.TitanGraph;
import com.thinkaurelius.titan.core.TitanTransaction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Features;
import com.tinkerpop.blueprints.GraphQuery;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.util.ElementHelper;
import com.tinkerpop.blueprints.util.StringFactory;
import com.tinkerpop.blueprints.util.wrappers.WrappedGraphQuery;
import com.tinkerpop.blueprints.util.wrappers.event.EventGraph;
import com.tinkerpop.blueprints.util.wrappers.event.EventTrigger;
import com.tinkerpop.blueprints.util.wrappers.event.listener.EdgeAddedEvent;
import com.tinkerpop.blueprints.util.wrappers.event.listener.EdgeRemovedEvent;
import com.tinkerpop.blueprints.util.wrappers.event.listener.GraphChangedListener;
import com.tinkerpop.blueprints.util.wrappers.event.listener.VertexAddedEvent;
import com.tinkerpop.blueprints.util.wrappers.event.listener.VertexRemovedEvent;

public class EventGraphWithMultiThreadedTitanTransactions extends EventGraph<TitanTransaction>
{
    private ReentrantReadWriteLock transactionLock = new ReentrantReadWriteLock();
    protected EventTrigger trigger;
    private TitanGraph titanGraph;
    protected TitanTransaction currentTxn;
    private int transactionID = 1;
    private Features features;

    protected final List<GraphChangedListener> graphChangedListeners = new ArrayList<GraphChangedListener>();

    public EventGraphWithMultiThreadedTitanTransactions(final TitanGraph titanGraph)
    {
        super(titanGraph.newTransaction());
        acquireTxnWriteLock();
        try
        {
            this.currentTxn = super.getBaseGraph();
            this.titanGraph = titanGraph;

            this.trigger = new EventTrigger(this, false);
        }
        finally
        {
            releaseTxnWriteLock();
        }
    }

    void acquireTxnWriteLock()
    {
        transactionLock.writeLock().lock();
    }

    void releaseTxnWriteLock()
    {
        transactionLock.writeLock().unlock();
    }

    void acquireTxnReadLock()
    {
        transactionLock.readLock().lock();
    }

    void releaseTxnReadLock()
    {
        transactionLock.readLock().unlock();
    }

    private void setCurrentTransaction(TitanTransaction txn)
    {
        acquireTxnWriteLock();
        try
        {
            this.currentTxn = txn;
            this.features = this.currentTxn.getFeatures().copyFeatures();
            this.features.isWrapper = true;
        }
        finally
        {
            releaseTxnWriteLock();
        }
    }

    public void commit()
    {
        acquireTxnWriteLock();
        try
        {
            this.currentTxn.commit();
            setCurrentTransaction(this.titanGraph.newTransaction());
            this.transactionID++;
        }
        finally
        {
            releaseTxnWriteLock();
        }
    }

    int getTransactionID()
    {
        return this.transactionID;
    }

    public void removeAllListeners()
    {
        acquireTxnReadLock();
        try
        {
            this.graphChangedListeners.clear();
        }
        finally
        {
            releaseTxnReadLock();
        }
    }

    public void addListener(final GraphChangedListener listener)
    {
        acquireTxnReadLock();
        try
        {
            this.graphChangedListeners.add(listener);
        }
        finally
        {
            releaseTxnReadLock();
        }
    }

    public Iterator<GraphChangedListener> getListenerIterator()
    {
        acquireTxnReadLock();
        try
        {
            return this.graphChangedListeners.iterator();
        }
        finally
        {
            releaseTxnReadLock();
        }
    }

    public EventTrigger getTrigger()
    {
        acquireTxnReadLock();
        try
        {
            return this.trigger;
        }
        finally
        {
            releaseTxnReadLock();
        }
    }

    public void removeListener(final GraphChangedListener listener)
    {
        acquireTxnReadLock();
        try
        {
            this.graphChangedListeners.remove(listener);
        }
        finally
        {
            releaseTxnReadLock();
        }
    }

    protected void onVertexAdded(Vertex vertex)
    {
        acquireTxnReadLock();
        try
        {
            this.trigger.addEvent(new VertexAddedEvent(vertex));
        }
        finally
        {
            releaseTxnReadLock();
        }
    }

    protected void onVertexRemoved(final Vertex vertex, Map<String, Object> props)
    {
        acquireTxnReadLock();
        try
        {
            this.trigger.addEvent(new VertexRemovedEvent(vertex, props));
        }
        finally
        {
            releaseTxnReadLock();
        }
    }

    protected void onEdgeAdded(Edge edge)
    {
        acquireTxnReadLock();
        try
        {
            this.trigger.addEvent(new EdgeAddedEvent(edge));
        }
        finally
        {
            releaseTxnReadLock();
        }
    }

    protected void onEdgeRemoved(final Edge edge, Map<String, Object> props)
    {
        acquireTxnReadLock();
        try
        {
            this.trigger.addEvent(new EdgeRemovedEvent(edge, props));
        }
        finally
        {
            releaseTxnReadLock();
        }
    }

    /**
     * Raises a vertexAdded event.
     */
    public Vertex addVertex(final Object id)
    {
        acquireTxnReadLock();
        try
        {
            final Vertex vertex = this.currentTxn.addVertex(id);
            if (vertex == null)
            {
                return null;
            }
            else
            {
                this.onVertexAdded(vertex);
                return new EventVertex(vertex, this);
            }
        }
        finally
        {
            releaseTxnReadLock();
        }
    }

    public Vertex getVertex(final Object id)
    {
        acquireTxnReadLock();
        try
        {
            final Vertex vertex = this.currentTxn.getVertex(id);
            if (vertex == null)
            {
                return null;
            }
            else
            {
                return new EventVertex(vertex, this);
            }
        }
        finally
        {
            releaseTxnReadLock();
        }
    }

    /**
     * Raises a vertexRemoved event.
     */
    public void removeVertex(final Vertex vertex)
    {
        acquireTxnReadLock();
        try
        {
            Vertex vertexToRemove = vertex;
            if (vertex instanceof EventVertex)
            {
                vertexToRemove = ((EventVertex) vertex).getBaseVertex();
            }

            Map<String, Object> props = ElementHelper.getProperties(vertex);
            this.currentTxn.removeVertex(vertexToRemove);
            this.onVertexRemoved(vertex, props);
        }
        finally
        {
            releaseTxnReadLock();
        }
    }

    public Iterable<Vertex> getVertices()
    {
        acquireTxnReadLock();
        try
        {
            return new EventVertexIterable(this.currentTxn.getVertices(), this);
        }
        finally
        {
            releaseTxnReadLock();
        }
    }

    public Iterable<Vertex> getVertices(final String key, final Object value)
    {
        acquireTxnReadLock();
        try
        {
            return new EventVertexIterable(this.currentTxn.getVertices(key, value), this);
        }
        finally
        {
            releaseTxnReadLock();
        }
    }

    /**
     * Raises an edgeAdded event.
     */
    public Edge addEdge(final Object id, final Vertex outVertex, final Vertex inVertex, final String label)
    {
        acquireTxnReadLock();
        try
        {
            Vertex outVertexToSet = outVertex;
            if (outVertex instanceof EventVertex)
            {
                outVertexToSet = ((EventVertex) outVertex).getBaseVertex();
            }

            Vertex inVertexToSet = inVertex;
            if (inVertex instanceof EventVertex)
            {
                inVertexToSet = ((EventVertex) inVertex).getBaseVertex();
            }

            final Edge edge = this.currentTxn.addEdge(id, outVertexToSet, inVertexToSet, label);
            if (edge == null)
            {
                return null;
            }
            else
            {
                this.onEdgeAdded(edge);
                return new EventEdge(edge, this);
            }
        }
        finally
        {
            releaseTxnReadLock();
        }
    }

    public Edge getEdge(final Object id)
    {
        acquireTxnReadLock();
        try
        {
            final Edge edge = this.currentTxn.getEdge(id);
            if (edge == null)
            {
                return null;
            }
            else
            {
                return new EventEdge(edge, this);
            }
        }
        finally
        {
            releaseTxnReadLock();
        }
    }

    /**
     * Raises an edgeRemoved event.
     */
    public void removeEdge(final Edge edge)
    {
        acquireTxnReadLock();
        try
        {
            Edge edgeToRemove = edge;
            if (edge instanceof EventEdge)
            {
                edgeToRemove = ((EventEdge) edge).getBaseEdge();
            }

            Map<String, Object> props = ElementHelper.getProperties(edge);
            this.currentTxn.removeEdge(edgeToRemove);
            this.onEdgeRemoved(edge, props);
        }
        finally
        {
            releaseTxnReadLock();
        }
    }

    public Iterable<Edge> getEdges()
    {
        acquireTxnReadLock();
        try
        {
            return new EventEdgeIterable(this.currentTxn.getEdges(), this);
        }
        finally
        {
            releaseTxnReadLock();
        }
    }

    public Iterable<Edge> getEdges(final String key, final Object value)
    {
        acquireTxnReadLock();
        try
        {
            return new EventEdgeIterable(this.currentTxn.getEdges(key, value), this);
        }
        finally
        {
            releaseTxnReadLock();
        }
    }

    public GraphQuery query()
    {
        acquireTxnReadLock();
        try
        {
            final EventGraphWithMultiThreadedTitanTransactions eventGraph = this;
            return new WrappedGraphQuery(this.currentTxn.query())
            {
                @Override
                public Iterable<Edge> edges()
                {
                    return new EventEdgeIterable(this.query.edges(), eventGraph);
                }

                @Override
                public Iterable<Vertex> vertices()
                {
                    return new EventVertexIterable(this.query.vertices(), eventGraph);
                }
            };
        }
        finally
        {
            releaseTxnReadLock();
        }
    }

    public void shutdown()
    {
        // acquireTxnWriteLock();
        try
        {
            try
            {
                this.titanGraph.shutdown();

                // TODO: hmmmmmm??
                this.trigger.fireEventQueue();
                this.trigger.resetEventQueue();
            }
            catch (Exception re)
            {

            }
        }
        finally
        {
            // releaseTxnWriteLock();
        }
    }

    public String toString()
    {
        acquireTxnReadLock();
        try
        {
            return StringFactory.graphString(this, this.currentTxn.toString());
        }
        finally
        {
            releaseTxnReadLock();
        }
    }

    @Override
    public TitanTransaction getBaseGraph()
    {
        acquireTxnReadLock();
        try
        {
            return this.currentTxn;
        }
        finally
        {
            releaseTxnReadLock();
        }
    }

    public Features getFeatures()
    {
        acquireTxnReadLock();
        try
        {
            return this.features;
        }
        finally
        {
            releaseTxnReadLock();
        }
    }

}
