package org.jboss.windup.graph.service;

import java.util.Iterator;

import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.dao.exception.NonUniqueResultException;
import org.jboss.windup.graph.model.WindupVertexFrame;

import com.thinkaurelius.titan.core.attribute.Text;
import com.tinkerpop.frames.FramedGraphQuery;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

public class GraphService<T extends WindupVertexFrame>
{
    private Class<T> type;
    private GraphContext context;

    public GraphService(GraphContext context, Class<T> type)
    {
        this.context = context;
        this.type = type;
    }

    public Iterable<T> findAll()
    {
        FramedGraphQuery query = context.getFramed().query();
        query.has(WindupVertexFrame.PROPERTY_TYPE, Text.CONTAINS, type.getAnnotation(TypeValue.class).value());
        return (Iterable<T>) query.vertices(type);
    }

    public Iterable<T> findByProperty(String key, Object value)
    {
        return context.getFramed().getVertices(key, value, type);
    }

    public T getUnique() throws NonUniqueResultException
    {
        Iterable<T> results = findAll();

        if (!results.iterator().hasNext())
        {
            return null;
        }

        Iterator<T> iter = results.iterator();
        T result = iter.next();

        if (iter.hasNext())
        {
            throw new NonUniqueResultException("Expected unique value, but returned non-unique.");
        }

        return result;
    }

    public T getUniqueByProperty(String property, Object value) throws NonUniqueResultException
    {
        Iterable<T> results = findByProperty(property, value);

        if (!results.iterator().hasNext())
        {
            return null;
        }

        Iterator<T> iter = results.iterator();
        T result = iter.next();

        if (iter.hasNext())
        {
            throw new NonUniqueResultException("Expected unique value, but returned non-unique.");
        }

        return result;
    }

    GraphContext getGraphContext()
    {
        return context;
    }
}
