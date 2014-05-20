package org.jboss.windup.graph.service;

import java.util.Iterator;

import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.dao.exception.NonUniqueResultException;
import org.jboss.windup.graph.model.WindupVertexFrame;

public class GraphService<T extends WindupVertexFrame>
{
    private Class<T> type;
    private GraphContext context;

    public GraphService(GraphContext context, Class<T> type)
    {
        this.context = context;
        this.type = type;
    }

    public Iterable<T> getByProperty(String key, Object value)
    {
        return context.getFramed().getVertices(key, value, type);
    }

    public T getByUniqueProperty(String property, Object value) throws NonUniqueResultException
    {
        Iterable<T> results = getByProperty(property, value);

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
}
