package org.jboss.windup.graph;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import com.tinkerpop.blueprints.VertexQuery;
import org.jboss.windup.graph.model.InMemoryVertexFrame;
import org.jboss.windup.util.exception.WindupException;

import org.apache.tinkerpop.gremlin.structure.Vertex;
import com.tinkerpop.frames.ClassUtilities;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.VertexFrame;

/**
 * This class represents the @Property elements of a framed element in memory.
 * 
 * This in memory representation can be attached to the graph by calling "attach(FramedGraph)"
 * 
 */
public class FramedElementInMemory<T extends VertexFrame> implements InvocationHandler
{
    private final Class<T> type;
    private final Object id;
    private final Map<String, Object> values = new HashMap<>();

    private static final Method attachMethod;
    private static final Method asVertexMethod;
    private static final Method hashCodeMethod;
    private static final Method equalsMethod;
    private static final Method toStringMethod;

    static
    {
        try
        {
            attachMethod = InMemoryVertexFrame.class.getMethod("attachToGraph", new Class[] { GraphContext.class });
            asVertexMethod = VertexFrame.class.getMethod("asVertex");
            hashCodeMethod = Object.class.getMethod("hashCode");
            equalsMethod = Object.class.getMethod("equals", new Class[] { Object.class });
            toStringMethod = Object.class.getMethod("toString");
        }
        catch (NoSuchMethodException e)
        {
            throw new WindupException(e.getMessage(), e);
        }
    }

    public FramedElementInMemory(Class<T> type)
    {
        this(type, null);
    }

    public FramedElementInMemory(Class<T> type, Object id)
    {
        this.type = type;
        this.id = id;
    }

    public Object invoke(final Object proxy, final Method method, final Object[] arguments)
    {
        String methodName = method.getName();
        if (method.equals(hashCodeMethod))
        {
            return this.hashCode();
        }
        else if (method.equals(equalsMethod))
        {
            return this.equals(arguments[0]);
        }
        else if (method.equals(toStringMethod))
        {
            return this.toString();
        }
        else if (method.equals(attachMethod))
        {
            attach((GraphContext)arguments[0]);
            return null;
        }
        else if (method.equals(asVertexMethod))
        {
            return asVertex();
        }

        final String propertyName;
        Property propertyAnnotation = method.getAnnotation(Property.class);
        if (propertyAnnotation == null)
        {
            Property windupPropertyAnnnotation = method.getAnnotation(Property.class);
            if (windupPropertyAnnnotation == null)
            {
                // if this is a getter or setter, try to find the property on the other method
                if (ClassUtilities.isGetMethod(method)) {
                    // get the setter
                    Method setMethod = ClassUtilities.getSetterMethodForGetter(method);
                    windupPropertyAnnnotation = setMethod.getAnnotation(Property.class);
                } else if (ClassUtilities.isSetMethod(method)) {
                    // get the getter
                    Method getMethod = ClassUtilities.getGetterMethodForSetter(method);
                    windupPropertyAnnnotation = getMethod.getAnnotation(Property.class);
                }

                if (windupPropertyAnnnotation == null)
                    throw new WindupException("Method " + methodName
                            + " called, but has no @Property annotation... only @Property methods are supported");
                else
                    propertyName = windupPropertyAnnnotation.value();
            }
            else
            {
                propertyName = windupPropertyAnnnotation.value();
            }
        }
        else
        {
            propertyName = propertyAnnotation.value();
        }

        if (ClassUtilities.isGetMethod(method))
        {
            return values.get(propertyName);
        }
        else if (ClassUtilities.isSetMethod(method))
        {
            Object value = arguments[0];
            if (value == null)
            {
                values.remove(propertyName);
            }
            else
            {
                values.put(propertyName, value);
            }
            return proxy;
        }
        else if (ClassUtilities.isRemoveMethod(method))
        {
            values.remove(propertyName);
            return proxy;
        }
        else
        {
            throw new WindupException("Unrecognized method " + methodName + " called on in-memory Frame!");
        }
    }

    private void attach(GraphContext context)
    {
        T element = context.getFramed().addVertex(null, this.type);
        Vertex v = element.asVertex();
        for (Map.Entry<String, Object> entry : values.entrySet())
        {
            v.setProperty(entry.getKey(), entry.getValue());
        }
    }

    private Vertex asVertex()
    {
        return new Vertex()
        {
            @Override
            public Iterable<Edge> getEdges(Direction direction, String... labels)
            {
                throw new RuntimeException("Method not supported for FramedElementInMemory");
            }

            @Override
            public Iterable<Vertex> getVertices(Direction direction, String... labels)
            {
                throw new RuntimeException("Method not supported for FramedElementInMemory");
            }

            @Override
            public VertexQuery query()
            {
                throw new RuntimeException("Method not supported for FramedElementInMemory");
            }

            @Override
            public Edge addEdge(String label, Vertex inVertex)
            {
                throw new RuntimeException("Method not supported for FramedElementInMemory");
            }

            @Override
            public <T> T getProperty(String key)
            {
                return (T)values.get(key);
            }

            @Override
            public Set<String> getPropertyKeys()
            {
                return values.keySet();
            }

            @Override
            public void setProperty(String key, Object value)
            {
                values.put(key, value);
            }

            @Override
            public <T> T removeProperty(String key)
            {
                return (T)values.remove(key);
            }

            @Override
            public void remove()
            {
                throw new RuntimeException("Method not supported for FramedElementInMemory");
            }

            @Override
            public Object getId()
            {
                return id;
            }
        };
    }

    @Override
    public String toString()
    {
        return "[Proxy for: " + type.getCanonicalName() + ", values: " + values + "]";
    }
}
