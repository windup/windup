package org.jboss.windup.graph.wrapper;

import java.util.Set;

import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.util.ElementHelper;
import com.tinkerpop.blueprints.util.wrappers.event.listener.EdgePropertyChangedEvent;
import com.tinkerpop.blueprints.util.wrappers.event.listener.EdgePropertyRemovedEvent;
import com.tinkerpop.blueprints.util.wrappers.event.listener.VertexPropertyChangedEvent;
import com.tinkerpop.blueprints.util.wrappers.event.listener.VertexPropertyRemovedEvent;

/**
 * An element with a GraphChangedListener attached. Those listeners are notified when changes occur to the properties of the element.
 *
 * @author Stephen Mallette
 */
public abstract class EventElement implements Element
{
    protected final EventGraphWithMultiThreadedTitanTransactions eventGraph;

    private int elementTxnId;
    private Element baseElement;

    protected EventElement(final Element baseElement, final EventGraphWithMultiThreadedTitanTransactions eventGraph)
    {
        eventGraph.acquireTxnReadLock();
        try
        {
            this.baseElement = baseElement;
            this.eventGraph = eventGraph;
            this.elementTxnId = eventGraph.getTransactionID();
        }
        finally
        {
            eventGraph.releaseTxnReadLock();
        }
    }

    protected void onVertexPropertyChanged(final Vertex vertex, final String key, final Object oldValue, final Object newValue)
    {
        this.eventGraph.getTrigger().addEvent(new VertexPropertyChangedEvent(vertex, key, oldValue, newValue));
    }

    protected void onEdgePropertyChanged(final Edge edge, final String key, final Object oldValue, final Object newValue)
    {
        this.eventGraph.getTrigger().addEvent(new EdgePropertyChangedEvent(edge, key, oldValue, newValue));
    }

    protected void onVertexPropertyRemoved(final Vertex vertex, final String key, final Object removedValue)
    {
        this.eventGraph.getTrigger().addEvent(new VertexPropertyRemovedEvent(vertex, key, removedValue));
    }

    protected void onEdgePropertyRemoved(final Edge edge, final String key, final Object removedValue)
    {
        this.eventGraph.getTrigger().addEvent(new EdgePropertyRemovedEvent(edge, key, removedValue));
    }

    public Set<String> getPropertyKeys()
    {
        eventGraph.acquireTxnReadLock();
        try
        {
            reloadElementIfNeeded();
            return this.baseElement.getPropertyKeys();
        }
        finally
        {
            eventGraph.releaseTxnReadLock();
        }
    }

    public Object getId()
    {
        return this.baseElement.getId();
    }

    /**
     * Raises a vertexPropertyRemoved or edgePropertyRemoved event.
     */
    public <T> T removeProperty(final String key)
    {
        eventGraph.acquireTxnReadLock();
        try
        {
            reloadElementIfNeeded();
            final Object propertyRemoved = baseElement.removeProperty(key);

            if (this instanceof Vertex)
            {
                this.onVertexPropertyRemoved((Vertex) this, key, propertyRemoved);
            }
            else if (this instanceof Edge)
            {
                this.onEdgePropertyRemoved((Edge) this, key, propertyRemoved);
            }

            return (T) propertyRemoved;
        }
        finally
        {
            eventGraph.releaseTxnReadLock();
        }
    }

    public <T> T getProperty(final String key)
    {
        eventGraph.acquireTxnReadLock();
        try
        {
            reloadElementIfNeeded();
            return this.baseElement.getProperty(key);
        }
        finally
        {
            eventGraph.releaseTxnReadLock();
        }
    }

    /**
     * Raises a vertexPropertyRemoved or edgePropertyChanged event.
     */
    public void setProperty(final String key, final Object value)
    {
        eventGraph.acquireTxnReadLock();
        try
        {
            reloadElementIfNeeded();
            final Object oldValue = this.baseElement.getProperty(key);
            this.baseElement.setProperty(key, value);

            if (this instanceof Vertex)
            {
                this.onVertexPropertyChanged((Vertex) this, key, oldValue, value);
            }
            else if (this instanceof Edge)
            {
                this.onEdgePropertyChanged((Edge) this, key, oldValue, value);
            }
        }
        finally
        {
            eventGraph.releaseTxnReadLock();
        }
    }

    public String toString()
    {
        return this.baseElement.toString();
    }

    public int hashCode()
    {
        return this.baseElement.hashCode();
    }

    public boolean equals(final Object object)
    {
        return ElementHelper.areEqual(this, object);
    }

    public Element getBaseElement()
    {
        eventGraph.acquireTxnReadLock();
        try
        {
            reloadElementIfNeeded();
            return this.baseElement;
        }
        finally
        {
            eventGraph.releaseTxnReadLock();
        }
    }

    public void remove()
    {
        eventGraph.acquireTxnReadLock();
        try
        {
            reloadElementIfNeeded();
            if (this instanceof Vertex)
                this.eventGraph.removeVertex((Vertex) this);
            else
                this.eventGraph.removeEdge((Edge) this);
        }
        finally
        {
            eventGraph.releaseTxnReadLock();
        }
    }

    void reloadElementIfNeeded()
    {
        this.eventGraph.acquireTxnReadLock();
        try
        {
            if (this.elementTxnId != this.eventGraph.getTransactionID())
            {
                if (this instanceof Vertex)
                {
                    this.baseElement = this.eventGraph.getBaseGraph().getVertex(this.baseElement.getId());
                }
                else
                {
                    this.baseElement = this.eventGraph.getBaseGraph().getEdge(this.baseElement.getId());
                }
            }
        }
        finally
        {
            this.eventGraph.releaseTxnReadLock();
        }
    }
}
